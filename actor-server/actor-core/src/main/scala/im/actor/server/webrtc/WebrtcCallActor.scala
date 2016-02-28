package im.actor.server.webrtc

import akka.actor._
import akka.http.scaladsl.util.FastFuture
import akka.pattern.pipe
import com.relayrides.pushy.apns.util.{ ApnsPayloadBuilder, SimpleApnsPushNotification }
import im.actor.api.rpc._
import im.actor.api.rpc.messaging.{ ApiServiceExPhoneCall, ApiServiceMessage }
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.api.rpc.webrtc._
import im.actor.concurrent.{ ActorStashing, FutureExt }
import im.actor.server.db.DbExtension
import im.actor.server.dialog.DialogExtension
import im.actor.server.eventbus.{ EventBus, EventBusExtension }
import im.actor.server.group.GroupExtension
import im.actor.server.model.{ Peer, PeerType }
import im.actor.server.sequence.{ ApplePushExtension, WeakUpdatesExtension }
import im.actor.server.user.UserExtension
import im.actor.server.values.ValuesExtension
import im.actor.types._

import scala.concurrent.Future
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

  private val weakUpdExt = WeakUpdatesExtension(context.system)
  private val dialogExt = DialogExtension(context.system)
  private val eventBusExt = EventBusExtension(context.system)
  private val userExt = UserExtension(context.system)
  private val groupExt = GroupExtension(context.system)
  private val valuesExt = ValuesExtension(context.system)
  private val apnsExt = ApplePushExtension(context.system)

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

  object Pair {
    def apply(d1: EventBus.DeviceId, d2: EventBus.DeviceId) = {
      require(d1 != d2)
      if (d1 < d2) new Pair(d1, d2)
      else new Pair(d2, d1)
    }
  }
  class Pair private (val left: EventBus.DeviceId, val right: EventBus.DeviceId)

  type SessionId = Long

  private var scheduledUpds = Map.empty[UserId, Cancellable]
  private var devices = Map.empty[EventBus.DeviceId, Device]
  private var clients = Map.empty[EventBus.Client, EventBus.DeviceId]
  private var participants = Map.empty[UserId, ApiCallMemberState.Value]
  private var sessions = Map.empty[Pair, SessionId]
  private var peer = Peer()
  private var callerUserId: Int = _

  def receive = waitForStart

  def waitForStart: Receive = {
    case s: StartCall ⇒
      case class Res(callees: Seq[Int], callerDeviceId: EventBus.DeviceId)
      this.peer = s.peer
      this.callerUserId = s.callerUserId

      (for {
        callees ← fetchParticipants(callerUserId, peer) map (_ filterNot (_ == callerUserId))
        callerDeviceId ← eventBusExt.fetchOwner(s.eventBusId)
        _ ← eventBusExt.join(EventBus.InternalClient(self), s.eventBusId, None)
        _ ← scheduleIncomingCallUpdates(callees)
      } yield Res(callees, callerDeviceId)) pipeTo self

      becomeStashing(replyTo ⇒ {
        case Res(callees, callerDeviceId) ⇒
          replyTo ! StartCallAck

          eventBusExt.post(EventBus.InternalClient(self), s.eventBusId, Seq(callerDeviceId), ApiAdvertiseMaster.toByteArray)

          callees foreach (putParticipant(_, ApiCallMemberState.RINGING))
          putParticipant(callerUserId, ApiCallMemberState.CONNECTED)
          broadcastSyncedSet()

          context become callInProgress(peer, s.eventBusId, callerDeviceId, System.currentTimeMillis(), callerUserId)
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
    callerUserId:   Int
  ): Receive = {
    def end(): Unit = {
      val duration = ((System.currentTimeMillis() - startTime) / 1000).toInt
      val randomId = ThreadLocalRandom.current().nextLong()
      val smsg = ApiServiceMessage("Call ended", Some(ApiServiceExPhoneCall(duration)))

      (for {
        _ ← if (peer.`type`.isPrivate) FutureExt.ftraverse(participants.keySet.toSeq)(userId ⇒ dialogExt.sendMessage(
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

    def connect(device: Device, pairDevice: Device): SessionId = {
      val sessionId = Random.nextLong()
      eventBusExt.post(
        EventBus.InternalClient(self),
        eventBusId,
        Seq(device.deviceId),
        ApiNeedOffer(pairDevice.deviceId, sessionId, pairDevice.peerSettings).toByteArray
      )
      sessions += Pair(device.deviceId, pairDevice.deviceId) → sessionId
      sessionId
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
                val sessionId =
                  sessions.getOrElse(Pair(device.deviceId, pairDevice.deviceId), connect(device, pairDevice))

                eventBusExt.post(
                  EventBus.InternalClient(self),
                  eventBusId,
                  Seq(device.deviceId),
                  ApiEnableConnection(pairDevice.deviceId, sessionId).toByteArray
                )
                eventBusExt.post(
                  EventBus.InternalClient(self),
                  eventBusId,
                  Seq(pairDevice.deviceId),
                  ApiEnableConnection(device.deviceId, sessionId).toByteArray
                )
            }

            if (!isConnected(userId)) {
              putParticipant(userId, ApiCallMemberState.CONNECTING)
              broadcastSyncedSet()
            }

            sender() ! JoinCallAck
          case None ⇒
            sender() ! Status.Failure(WebrtcCallErrors.NotJoinedToEventBus)
        }
      case RejectCall(userId, authId) ⇒
        cancelIncomingCallUpdates(userId)
        weakUpdExt.broadcastUserWeakUpdate(userId, UpdateCallHandled(id), excludeAuthIds = Set(authId))
        broadcastSyncedSet()
        sender() ! RejectCallAck
      case GetInfo ⇒
        sender() ! GetInfoAck(eventBusId, callerUserId, participants.keySet.toSeq)
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

              for {
                userId ← ebMessage.client.externalUserId
                state ← participants.get(userId)
              } yield if (state == ApiCallMemberState.RINGING) putParticipant(userId, ApiCallMemberState.RINGING_REACHED)
            }
          case msg: ApiNegotinationSuccessful ⇒
            ebMessage.client.externalUserId foreach { userId ⇒
              putParticipant(userId, ApiCallMemberState.CONNECTED)
              broadcastSyncedSet()
            }
          case msg: ApiOnRenegotiationNeeded ⇒
            // TODO: #perf remove sessions.find and sessions.filterNot
            for {
              deviceId ← ebMessage.deviceId
              (pair, sessionId) ← sessions find (_ == msg.sessionId)
              leftDevice ← devices get pair.left
              rightDevice ← devices get pair.right
            } yield {
              if (pair == Pair(deviceId, msg.device)) {
                sessions = sessions filterNot (_ == sessionId)
                eventBusExt.post(EventBus.InternalClient(self), eventBusId, Seq(pair.left), ApiCloseSession(pair.right, sessionId).toByteArray)
                eventBusExt.post(EventBus.InternalClient(self), eventBusId, Seq(pair.right), ApiCloseSession(pair.left, sessionId).toByteArray)
                connect(leftDevice, rightDevice)
              } else log.warning("Received OnRenegotiationNeeded for a wrong deviceId")
            }
          case _ ⇒
        }
      case EventBus.Disconnected(_, client, deviceId) ⇒
        removeDevice(deviceId)
        client.externalUserId foreach { userId ⇒
          putParticipant(userId, ApiCallMemberState.ENDED)
          broadcastSyncedSet()
        }
      case EventBus.Disposed(_) ⇒
        end()
        deleteSyncedSet()
      case _: StartCall ⇒ sender() ! WebrtcCallErrors.CallAlreadyStarted
    }
  }

  private def isConnected(userId: UserId): Boolean = {
    val userDevices = devices.filter(_._2.client.externalUserId.contains(userId)).values.map(_.deviceId).toSet
    sessions.keySet.exists(pair ⇒ userDevices.contains(pair.left) || userDevices.contains(pair.right))
  }

  private def putParticipant(userId: Int, state: ApiCallMemberState.Value): Unit = {
    participants get userId match {
      case Some(oldState) ⇒
        if (oldState != state) {
          log.debug("Changing participant {} state from {} to {}", userId, oldState, state)
          participants += userId → state
        } else log.error("Attempt to change participant state to the same value {}", state)
      case None ⇒
        log.error("Attempt to change state of a non-participant {}", userId)
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

  private def scheduleIncomingCallUpdates(callees: Seq[UserId]): Future[Unit] = {
    for {
      authIdsMap ← userExt.getAuthIdsMap(callees.toSet)
      credsMap ← FutureExt.ftraverse(authIdsMap.toSeq) {
        case (userId, authIds) ⇒
          apnsExt.findVoipCreds(authIds.toSet) map (userId → _)
      }
    } yield {
      scheduledUpds =
        credsMap
          .map {
            case (userId, creds) ⇒
              (
                userId,
                context.system.scheduler.schedule(0.seconds, 5.seconds) {
                  weakUpdExt.broadcastUserWeakUpdate(userId, UpdateIncomingCall(id), reduceKey = Some(s"call_$id"))

                  val payload = (new ApnsPayloadBuilder).addCustomProperty("callId", id).buildWithDefaultMaximumLength()

                  val instanceCreds = creds flatMap (c ⇒ apnsExt.getVoipInstance(c.apnsKey) map (_ → c))
                  for ((instance, cred) ← instanceCreds) {
                    val notif = new SimpleApnsPushNotification(cred.token.toByteArray, payload)
                    instance.getQueue.add(notif)
                  }
                }
              )
          }
          .toMap
    }
  }

  private def cancelIncomingCallUpdates(callee: UserId) =
    scheduledUpds get callee foreach { c ⇒
      c.cancel()
      scheduledUpds -= callee
    }

  private def broadcastSyncedSet(): Unit = {
    val activeCall =
      ApiActiveCall(id, peer.asStruct, participants.toVector map {
        case (userId, state) ⇒

          ApiCallMember(userId, ApiCallMemberStateHolder(
            state = state,
            fallbackIsRinging = Some(state == ApiCallMemberState.RINGING),
            fallbackIsConnected = Some(state == ApiCallMemberState.CONNECTED),
            fallbackIsConnecting = Some(state == ApiCallMemberState.CONNECTING),
            fallbackIsRingingReached = Some(state == ApiCallMemberState.RINGING_REACHED),
            fallbackIsEnded = Some(state == ApiCallMemberState.ENDED)
          ))
      }).toByteArray
    participants.keySet foreach (valuesExt.syncedSet.put(_, Webrtc.SyncedSetName, id, activeCall))
  }

  private def deleteSyncedSet(): Unit =
    participants.keySet foreach { userId ⇒
      valuesExt.syncedSet.delete(userId, Webrtc.SyncedSetName, id)
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