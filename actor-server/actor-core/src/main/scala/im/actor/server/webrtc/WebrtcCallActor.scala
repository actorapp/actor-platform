package im.actor.server.webrtc

import akka.actor._
import akka.http.scaladsl.util.FastFuture
import akka.pattern.pipe
import com.relayrides.pushy.apns.util.{ ApnsPayloadBuilder, SimpleApnsPushNotification }
import im.actor.api.rpc._
import im.actor.api.rpc.messaging.{ ApiServiceExPhoneCall, ApiServiceExPhoneMissed, ApiServiceMessage }
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.api.rpc.webrtc._
import im.actor.concurrent.{ StashingActor, FutureExt }
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

private[webrtc] sealed trait WebrtcCallMessage

private[webrtc] object WebrtcCallMessages {
  final case class StartCall(callerUserId: UserId, callerAuthId: AuthId, peer: Peer, timeout: Option[Long]) extends WebrtcCallMessage
  final case class StartCallAck(eventBusId: String, callerDeviceId: EventBus.DeviceId)

  final case class JoinCall(calleeUserId: UserId, authId: AuthId) extends WebrtcCallMessage
  case object JoinCallAck

  final case class RejectCall(calleeUserId: UserId, authId: AuthId) extends WebrtcCallMessage
  case object RejectCallAck

  case object GetInfo extends WebrtcCallMessage
  final case class GetInfoAck(eventBusId: String, peer: Peer, participantUserIds: Seq[UserId]) {
    val tupled = (eventBusId, peer, participantUserIds)
  }

  private[webrtc] case class SendIncomingCall(userId: UserId)
}

private[webrtc] final case class WebrtcCallEnvelope(id: Long, message: WebrtcCallMessage)

object WebrtcCallActor {
  val RegionTypeName = "WebrtcCall"

  def props = Props(classOf[WebrtcCallActor])
}

private trait Members {
  this: ActorLogging ⇒

  private var members = Map.empty[UserId, Member]

  sealed trait MemberState

  object MemberStates {
    object Ringing extends MemberState
    object RingingReached extends MemberState
    object Connecting extends MemberState
    object Connected extends MemberState
    object Ended extends MemberState
  }

  case class Member(userId: UserId, state: MemberState, isJoined: Boolean) {
    import MemberStates._

    lazy val apiState = state match {
      case Ringing        ⇒ ApiCallMemberState.RINGING
      case RingingReached ⇒ ApiCallMemberState.RINGING_REACHED
      case Connecting     ⇒ ApiCallMemberState.CONNECTING
      case Connected      ⇒ ApiCallMemberState.CONNECTED
      case Ended          ⇒ ApiCallMemberState.ENDED
    }
  }

  def addMember(userId: UserId, initState: MemberState, isJoined: Boolean = false): Unit = {
    members get userId match {
      case Some(_) ⇒ throw new RuntimeException("Attempt to add already existing member")
      case None ⇒
        val member = Member(userId, initState, isJoined)
        log.debug("Adding member: {}", member)
        members += (userId → member)
    }
  }

  def setMemberJoined(userId: UserId, isJoined: Boolean = true): Unit =
    members get userId match {
      case Some(member) if isJoined != member.isJoined ⇒ members += userId → member.copy(isJoined = isJoined)
      case Some(_) ⇒
        throw new RuntimeException(s"Attempt to set member joined to $isJoined who is already $isJoined")
      case None ⇒
        throw new RuntimeException("Attempt to set an unexistent member joined")
    }

  def setMemberState(userId: UserId, state: MemberState): Unit =
    members get userId match {
      case Some(member) ⇒
        log.debug("Changing member[userId: {}] state from: {} to: {}", userId, member.state, state)
        members += userId → member.copy(state = state)
      case None ⇒ throw new RuntimeException("Attempt to change an unexistend member state")
    }

  def getMember(userId: UserId) = members get userId

  def memberUserIds = members.keySet

  def getMembers = members

  def everyoneRejected(callerUserId: Int) =
    members.values
      .filterNot(_.userId == callerUserId)
      .filterNot(_.state == MemberStates.Ended)
      .isEmpty

  def everyoneLeft(callerUserId: Int) = {
    val ringing = members.values.filter(_.state == MemberStates.Ringing)
    val joined = members.values.filter(_.isJoined)

    joined.size <= 1 && ringing.isEmpty
  }
}

private final class WebrtcCallActor extends StashingActor with ActorLogging with Members {
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
  private val webrtcExt = WebrtcExtension(context.system)

  case class Device(
    deviceId:     EventBus.DeviceId,
    client:       EventBus.Client,
    peerSettings: Option[ApiPeerSettings],
    isJoined:     Boolean
  ) {
    def canPreConnect(pairPeerSettings: Option[ApiPeerSettings]): Boolean =
      isJoined ||
        (peerSettings.map(_.canPreConnect).isDefined && pairPeerSettings.map(_.canPreConnect).isDefined)
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

  private val eventBusClient = EventBus.InternalClient(self)

  private var scheduledUpds = Map.empty[UserId, Cancellable]
  private var devices = Map.empty[EventBus.DeviceId, Device]
  private var clients = Map.empty[EventBus.Client, EventBus.DeviceId]
  private var sessions = Map.empty[Pair, SessionId]
  private var isConversationStarted: Boolean = false
  private var peer = Peer()
  private var callerUserId: Int = _

  def receive = waitForStart

  // FIXME: set receive timeout

  def waitForStart: Receive = {
    case s: StartCall ⇒
      case class Res(eventBusId: String, callees: Seq[Int], callerDeviceId: EventBus.DeviceId)
      this.peer = s.peer
      this.callerUserId = s.callerUserId

      (for {
        callees ← fetchParticipants(callerUserId, peer) map (_ filterNot (_ == callerUserId))
        eventBusId ← eventBusExt.create(eventBusClient, timeout = None, isOwned = Some(true)) map (_._1)
        callerDeviceId ← eventBusExt.join(EventBus.ExternalClient(s.callerUserId, s.callerAuthId), eventBusId, s.timeout)
        _ ← scheduleIncomingCallUpdates(callees)
      } yield Res(eventBusId, callees, callerDeviceId)) pipeTo self

      becomeStashing(replyTo ⇒ {
        case Res(eventBusId, callees, callerDeviceId) ⇒
          replyTo ! StartCallAck(eventBusId, callerDeviceId)

          advertiseMaster(eventBusId, callerDeviceId)

          callees foreach (userId ⇒ addMember(userId, MemberStates.Ringing))
          addMember(callerUserId, MemberStates.Connected, isJoined = true)
          broadcastSyncedSet()

          context become callInProgress(peer, eventBusId, callerDeviceId, System.currentTimeMillis(), callerUserId)
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

      val smsg =
        if (isConversationStarted) ApiServiceMessage("Call ended", Some(ApiServiceExPhoneCall(duration)))
        else ApiServiceMessage("Missed call", Some(ApiServiceExPhoneMissed))

      (for {
        _ ← if (peer.`type`.isPrivate) FutureExt.ftraverse(memberUserIds.toSeq)(userId ⇒ dialogExt.sendMessage(
          peer = ApiPeer(ApiPeerType.Private, callerUserId),
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
            setMemberJoined(userId)
            cancelIncomingCallUpdates(userId)

            weakUpdExt.broadcastUserWeakUpdate(userId, UpdateCallHandled(id), excludeAuthIds = Set(authId))

            val connectedDevices =
              devices.view filterNot (_._1 == device.deviceId) map (_._2) filter (_.isJoined) map {
                case pairDevice ⇒
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
                  pairDevice
              }

            if (connectedDevices.force.nonEmpty)
              this.isConversationStarted = true

            if (!isConnected(userId)) {
              setMemberState(userId, MemberStates.Connecting)
              broadcastSyncedSet()
            }

            sender() ! JoinCallAck
          case None ⇒
            sender() ! Status.Failure(WebrtcCallErrors.NotJoinedToEventBus)
        }
      case RejectCall(userId, authId) ⇒
        cancelIncomingCallUpdates(userId)
        setMemberState(userId, MemberStates.Ended)
        val client = EventBus.ExternalClient(userId, authId)
        for (deviceId ← clients get client) {
          clients -= client
          devices -= deviceId
        }
        weakUpdExt.broadcastUserWeakUpdate(userId, UpdateCallHandled(id), excludeAuthIds = Set(authId))
        broadcastSyncedSet()
        sender() ! RejectCallAck

        if ( // If caller changed his mind until anyone picked up
        (!this.isConversationStarted && userId == callerUserId) ||
          // If everyone rejected dialing, there will no any conversation ;(
          (!this.isConversationStarted && everyoneRejected(callerUserId))) end()
      case GetInfo ⇒
        if (peer.typ.isPrivate) {
          sender() ! GetInfoAck(eventBusId, Peer(PeerType.Private, memberUserIds.filterNot(_ == peer.id).head), memberUserIds.toSeq)
        } else {
          sender() ! GetInfoAck(eventBusId, peer, memberUserIds.toSeq)
        }
      case EventBus.Joined(_, client, deviceId) ⇒
        if (client.isExternal)
          advertiseMaster(eventBusId, deviceId)
      case ebMessage: EventBus.Message ⇒
        ApiWebRTCSignaling.parseFrom(ebMessage.message).right foreach {
          case msg: ApiAdvertiseSelf ⇒
            for (deviceId ← ebMessage.deviceId) yield {
              val newDevice = Device(deviceId, ebMessage.client, msg.peerSettings, isJoined = deviceId == callerDeviceId)
              devices foreach {
                case (pairDeviceId, pairDevice) ⇒
                  if (pairDevice.canPreConnect(msg.peerSettings))
                    connect(newDevice, pairDevice)
              }
              putDevice(deviceId, ebMessage.client, newDevice)

              for {
                userId ← ebMessage.client.externalUserId
                member ← getMember(userId)
              } yield {
                if (member.state == MemberStates.Ringing)
                  setMemberState(userId, MemberStates.RingingReached)
              }
            }
          case msg: ApiNegotinationSuccessful ⇒
            ebMessage.client.externalUserId foreach { userId ⇒
              setMemberState(userId, MemberStates.Connected)
              broadcastSyncedSet()
            }
          case msg: ApiOnRenegotiationNeeded ⇒
            // TODO: #perf remove sessions.find and sessions.filterNot
            for {
              deviceId ← ebMessage.deviceId
              (pair, sessionId) ← sessions find (_._2 == msg.sessionId)
              leftDevice ← devices get pair.left
              rightDevice ← devices get pair.right
            } yield {
              val chkPair = Pair(deviceId, msg.device)
              if (pair.left == chkPair.left && pair.right == chkPair.right) {
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
          if (!devices.values.exists(_.client.externalUserId.contains(userId))) {
            setMemberState(userId, MemberStates.Ended)
            setMemberJoined(userId, isJoined = false)
            broadcastSyncedSet()
          }
        }

        if ( // no one have been reached and caller left
        (!isConversationStarted && client.externalUserId.contains(callerUserId)) ||
          // there is no one left who can have a conversation
          (isConversationStarted && everyoneLeft(callerUserId))) end()
      case EventBus.Disposed(_) ⇒
        end()
        deleteSyncedSet()
      case SendIncomingCall(userId) ⇒
        if (System.currentTimeMillis() - startTime < 30000)
          weakUpdExt.broadcastUserWeakUpdate(userId, UpdateIncomingCall(id), reduceKey = Some(s"call_$id"))
        else {
          cancelIncomingCallUpdates(userId)
          setMemberState(userId, MemberStates.Ended)
          for {
            deviceId ← clients.filter(_._1.externalUserId.contains(userId)).map(_._2)
          } yield removeDevice(deviceId)
          broadcastSyncedSet()
        }
      case _: StartCall ⇒ sender() ! WebrtcCallErrors.CallAlreadyStarted
    }
  }

  private def advertiseMaster(eventBusId: String, deviceId: EventBus.DeviceId): Unit = {
    val advMaster =
      ApiAdvertiseMaster(
        server = webrtcExt.config.iceServers.toVector map (s ⇒ ApiICEServer(s.url, s.username, s.credential))
      )
    eventBusExt.post(EventBus.InternalClient(self), eventBusId, Seq(deviceId), advMaster.toByteArray)
  }

  private def isConnected(userId: UserId): Boolean = {
    val userDevices = devices.filter(_._2.client.externalUserId.contains(userId)).values.map(_.deviceId).toSet
    sessions.keySet.exists(pair ⇒ userDevices.contains(pair.left) || userDevices.contains(pair.right))
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
      authIds ← userExt.getAuthIds(callees.toSet)
      credss ← apnsExt.findVoipCreds(authIds.toSet)
    } yield {
      credss foreach { creds ⇒
        val payload = (new ApnsPayloadBuilder).addCustomProperty("callId", id).buildWithDefaultMaximumLength()

        val instanceCreds = apnsExt.getVoipInstance(creds.apnsKey) map (_ → creds)
        for ((instance, cred) ← instanceCreds) {
          val notif = new SimpleApnsPushNotification(cred.token.toByteArray, payload)
          instance.getQueue.add(notif)
        }
      }

      scheduledUpds =
        callees.map { userId ⇒
          (
            userId,
            context.system.scheduler.schedule(0.seconds, 5.seconds, self, SendIncomingCall(userId))
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
      ApiActiveCall(id, peer.asStruct, getMembers.toVector map {
        case (userId, member) ⇒
          val state = member.apiState

          ApiCallMember(userId, ApiCallMemberStateHolder(
            state = state,
            fallbackIsRinging = Some(state == ApiCallMemberState.RINGING),
            fallbackIsConnected = Some(state == ApiCallMemberState.CONNECTED),
            fallbackIsConnecting = Some(state == ApiCallMemberState.CONNECTING),
            fallbackIsRingingReached = Some(state == ApiCallMemberState.RINGING_REACHED),
            fallbackIsEnded = Some(state == ApiCallMemberState.ENDED)
          ))
      }).toByteArray
    memberUserIds foreach (valuesExt.syncedSet.put(_, Webrtc.SyncedSetName, id, activeCall))
  }

  private def deleteSyncedSet(): Unit =
    memberUserIds foreach { userId ⇒
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