package im.actor.server.webrtc

import akka.actor._
import akka.http.scaladsl.util.FastFuture
import akka.pattern.pipe
import im.actor.api.rpc._
import im.actor.api.rpc.messaging.{ ApiServiceExPhoneCall, ApiServiceMessage }
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.api.rpc.webrtc._
import im.actor.concurrent.{ ActorStashing, FutureExt }
import im.actor.server.dialog.DialogExtension
import im.actor.server.eventbus.{ EventBus, EventBusExtension }
import im.actor.server.group.GroupExtension
import im.actor.server.model.{ Peer, PeerType }
import im.actor.server.sequence.WeakUpdatesExtension
import im.actor.types._

import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.util.Random

sealed abstract class WebrtcCallError(message: String) extends RuntimeException(message)

object WebrtcCallErrors {
  object NotAParticipant extends WebrtcCallError("Not participant")
  object CallNotStarted extends WebrtcCallError("Call not started")
  object CallAlreadyStarted extends WebrtcCallError("Call already started")
  object NotJoinedToEventBus extends WebrtcCallError("Not joined to EventBus")
}

sealed trait WebrtcCallMessage

object WebrtcCallMessages {
  final case class StartCall(callerUserId: UserId, peer: Peer, eventBusId: String) extends WebrtcCallMessage
  case object StartCallAck

  final case class JoinCall(calleeUserId: UserId, authId: AuthId) extends WebrtcCallMessage
  case object JoinCallAck

  final case class RejectCall(calleeUserId: UserId, authId: AuthId) extends WebrtcCallMessage
  case object RejectCallAck

  case object GetInfo extends WebrtcCallMessage
  final case class GetInfoAck(eventBusId: String, callerUserId: UserId, participantUserIds: Seq[UserId]) {
    val tupled = (eventBusId, callerUserId, participantUserIds)
  }
}

final case class WebrtcCallEnvelope(id: Long, message: WebrtcCallMessage)

object WebrtcCallActor {
  val RegionTypeName = "WebrtcCall"

  def props = Props(classOf[WebrtcCallActor])
}

private final class WebrtcCallActor extends ActorStashing with ActorLogging {
  import WebrtcCallMessages._
  import context.dispatcher

  private val id = self.path.name.toLong

  private lazy val weakUpdExt = WeakUpdatesExtension(context.system)
  private lazy val dialogExt = DialogExtension(context.system)
  private lazy val eventBusExt = EventBusExtension(context.system)
  private lazy val groupExt = GroupExtension(context.system)

  case class Device(
    deviceId:     EventBus.DeviceId,
    client:       EventBus.Client,
    peerSettings: Option[ApiPeerSettings],
    isJoined:     Boolean
  ) {
    def canConnect(pairPeerSettings: Option[ApiPeerSettings]): Boolean =
      isJoined ||
        (peerSettings.map(_.canConnect).isDefined && pairPeerSettings.map(_.canConnect).isDefined)
  }

  private var scheduledUpds = Map.empty[UserId, Cancellable]
  private var devices = Map.empty[EventBus.DeviceId, Device]
  private var clients = Map.empty[EventBus.Client, EventBus.DeviceId]

  def receive = waitForStart

  def waitForStart: Receive = {
    case StartCall(callerUserId, peer, eventBusId) ⇒
      case class Res(callees: Seq[Int], callerDeviceId: EventBus.DeviceId)

      (for {
        callees ← fetchParticipants(callerUserId, peer) map (_ filterNot (_ == callerUserId))
        callerDeviceId ← eventBusExt.fetchOwner(eventBusId)
        _ ← eventBusExt.join(EventBus.InternalClient(self), eventBusId, None)
      } yield Res(callees, callerDeviceId)) pipeTo self

      becomeStashing(replyTo ⇒ {
        case Res(callees, callerDeviceId) ⇒
          scheduleIncomingCallUpdates(callees)
          replyTo ! StartCallAck

          eventBusExt.post(EventBus.InternalClient(self), eventBusId, Seq(callerDeviceId), ApiAdvertiseMaster.toByteArray)

          context become callInProgress(peer, eventBusId, callerDeviceId, System.currentTimeMillis(), callerUserId, callees :+ callerUserId)
          unstashAll()
        case failure: Status.Failure ⇒
          replyTo forward failure
          context stop self
      }, discardOld = true)
    case _ ⇒ sender() ! Status.Failure(WebrtcCallErrors.CallNotStarted)
  }

  def callInProgress(
    peer:           Peer,
    eventBusId:     String,
    callerDeviceId: EventBus.DeviceId,
    startTime:      Long,
    callerUserId:   Int,
    participants:   Seq[Int]
  ): Receive = {
    def end(): Unit = {
      val duration = ((System.currentTimeMillis() - startTime) / 1000).toInt
      val randomId = ThreadLocalRandom.current().nextLong()
      val smsg = ApiServiceMessage("Call ended", Some(ApiServiceExPhoneCall(duration)))

      (for {
        _ ← if (peer.`type`.isPrivate) FutureExt.ftraverse(participants)(userId ⇒ dialogExt.sendMessage(
          peer = ApiPeer(ApiPeerType.Private, userId),
          senderUserId = callerUserId,
          senderAuthId = None,
          senderAuthSid = 0,
          randomId = randomId,
          message = smsg
        ))
        else dialogExt.sendMessage(
          peer = peer.asStruct,
          senderUserId = callerUserId,
          senderAuthId = None,
          senderAuthSid = 0,
          randomId = randomId,
          message = smsg
        )
      } yield PoisonPill) pipeTo self onFailure {
        case e ⇒
          log.error(e, "Failed to stop call")
          context stop self
      }
    }

    def connect(device: Device, pairDevice: Device): Unit = {
      eventBusExt.post(
        EventBus.InternalClient(self),
        eventBusId,
        Seq(device.deviceId),
        ApiNeedOffer(pairDevice.deviceId, Random.nextLong(), pairDevice.peerSettings).toByteArray
      )
    }

    {
      case JoinCall(userId, authId) ⇒
        val client = EventBus.ExternalClient(userId, authId)

        (for {
          deviceId ← clients get client
          device ← devices get deviceId
        } yield device) match {
          case Some(device) ⇒
            putDevice(device.deviceId, client, device.copy(isJoined = true))
            cancelIncomingCallUpdates(userId)
            weakUpdExt.broadcastUserWeakUpdate(userId, UpdateCallHandled(id), excludeAuthIds = Set(authId))
            devices.view filterNot (_._1 == device.deviceId) filter (_._2.isJoined) foreach {
              case (_, pairDevice) ⇒
                if (!device.canConnect(pairDevice.peerSettings)) // if we didn't connect them in AdvertiseSelf
                  connect(device, pairDevice)

                eventBusExt.post(
                  EventBus.InternalClient(self),
                  eventBusId,
                  Seq(device.deviceId),
                  ApiEnableConnection(pairDevice.deviceId, 1L).toByteArray
                )
                eventBusExt.post(
                  EventBus.InternalClient(self),
                  eventBusId,
                  Seq(pairDevice.deviceId),
                  ApiEnableConnection(device.deviceId, 1L).toByteArray
                )
            }
            sender() ! JoinCallAck
          case None ⇒
            sender() ! Status.Failure(WebrtcCallErrors.NotJoinedToEventBus)
        }
      case RejectCall(userId, authId) ⇒
        cancelIncomingCallUpdates(userId)
        weakUpdExt.broadcastUserWeakUpdate(userId, UpdateCallHandled(id), excludeAuthIds = Set(authId))
        sender() ! RejectCallAck
      case GetInfo ⇒
        sender() ! GetInfoAck(eventBusId, callerUserId, participants)
      case EventBus.Joined(_, client, deviceId) ⇒
        if (client.isExternal)
          eventBusExt.post(EventBus.InternalClient(self), eventBusId, Seq(deviceId), ApiAdvertiseMaster.toByteArray)
      case ebMessage: EventBus.Message ⇒
        ApiWebRTCSignaling.parseFrom(ebMessage.message).right foreach {
          case msg: ApiAdvertiseSelf ⇒
            for (deviceId ← ebMessage.deviceId) yield {
              val newDevice = Device(deviceId, ebMessage.client, msg.peerSettings, isJoined = deviceId == callerDeviceId)
              devices foreach {
                case (pairDeviceId, pairDevice) ⇒
                  if (pairDevice.canConnect(msg.peerSettings))
                    connect(newDevice, pairDevice)
              }
              putDevice(deviceId, ebMessage.client, newDevice)
            }
          case _ ⇒
        }
      case EventBus.Disconnected(_, client, deviceId) ⇒ removeDevice(deviceId)
      case EventBus.Disposed(_)                       ⇒ end()
      case _: StartCall                               ⇒ sender() ! WebrtcCallErrors.CallAlreadyStarted
    }
  }

  private def putDevice(deviceId: EventBus.DeviceId, client: EventBus.Client, device: Device): Unit = {
    devices += deviceId → device
    clients += client → deviceId
  }

  private def removeDevice(deviceId: EventBus.DeviceId): Option[Device] =
    devices get deviceId map { device ⇒
      devices -= deviceId
      clients -= device.client
      device
    }

  private def fetchParticipants(callerUserId: Int, peer: Peer) =
    peer match {
      case Peer(PeerType.Private, userId) ⇒ FastFuture.successful(Seq(callerUserId, userId))
      case Peer(PeerType.Group, groupId)  ⇒ groupExt.getMemberIds(groupId) map (_._1)
      case _                              ⇒ FastFuture.failed(new RuntimeException(s"Unknown peer type: ${peer.`type`}"))
    }

  private def scheduleIncomingCallUpdates(callees: Seq[UserId]): Unit =
    scheduledUpds =
      callees
        .map { callee ⇒
          (
            callee,
            context.system.scheduler.schedule(0.seconds, 5.seconds) {
              weakUpdExt.broadcastUserWeakUpdate(callee, UpdateIncomingCall(id), reduceKey = Some(s"call_$id"))
            }
          )
        }
        .toMap

  private def cancelIncomingCallUpdates(callee: UserId) =
    scheduledUpds get callee foreach { c ⇒
      c.cancel()
      scheduledUpds -= callee
    }

  override def postStop(): Unit = {
    scheduledUpds.values foreach (_.cancel())
    super.postStop()
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    log.error(reason, "Failure on message: {}", message)
  }
}