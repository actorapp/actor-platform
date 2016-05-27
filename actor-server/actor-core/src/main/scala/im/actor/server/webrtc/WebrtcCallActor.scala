package im.actor.server.webrtc

import akka.actor._
import akka.http.scaladsl.util.FastFuture
import akka.pattern.pipe
import com.relayrides.pushy.apns.util.ApnsPayloadBuilder
import im.actor.api.rpc._
import im.actor.api.rpc.messaging.{ ApiServiceExPhoneCall, ApiServiceExPhoneMissed, ApiServiceMessage }
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.api.rpc.webrtc._
import im.actor.concurrent.{ FutureExt, StashingActor }
import im.actor.server.dialog.{ DialogExtension, UserAcl }
import im.actor.server.eventbus.{ EventBus, EventBusExtension }
import im.actor.server.group.GroupExtension
import im.actor.server.model.{ Peer, PeerType }
import im.actor.server.push.actor.{ ActorPush, ActorPushMessage }
import im.actor.server.sequence._
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
  object CallForbidden extends WebrtcCallError("You are forbidden to call this user")
  object GroupTooBig extends WebrtcCallError("Group is too big for group call")
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

  case class Member(userId: UserId, state: MemberState, isJoined: Boolean, callAttempts: Int) {
    import MemberStates._

    lazy val apiState = state match {
      case Ringing        ⇒ ApiCallMemberState.RINGING
      case RingingReached ⇒ ApiCallMemberState.RINGING_REACHED
      case Connecting     ⇒ ApiCallMemberState.CONNECTING
      case Connected      ⇒ ApiCallMemberState.CONNECTED
      case Ended          ⇒ ApiCallMemberState.ENDED
    }
  }

  def addMember(userId: UserId, initState: MemberState, isJoined: Boolean = false, callAttempts: Int = 1): Unit = {
    members get userId match {
      case Some(_) ⇒ throw new RuntimeException("Attempt to add already existing member")
      case None ⇒
        val member = Member(userId, initState, isJoined, callAttempts)
        log.debug("Adding member: {}", member)
        members += (userId → member)
    }
  }

  def incrementMemberCallAttempt(userId: UserId): Unit =
    members get userId match {
      case Some(member) ⇒ members += userId → member.copy(callAttempts = member.callAttempts + 1)
      case None         ⇒ throw new RuntimeException("Attempt to increment callAttempts of a nonexistent member")
    }

  def setMemberJoined(userId: UserId, isJoined: Boolean = true): Unit =
    members get userId match {
      case Some(member) ⇒ members += userId → member.copy(isJoined = isJoined)
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

private final class WebrtcCallActor extends StashingActor with ActorLogging with Members with APNSSend with UserAcl {
  import WebrtcCallMessages._
  import context.dispatcher

  private val id = self.path.name.toLong
  protected implicit val system: ActorSystem = context.system

  private val weakUpdExt = WeakUpdatesExtension(system)
  private val dialogExt = DialogExtension(system)
  private val eventBusExt = EventBusExtension(system)
  private val userExt = UserExtension(system)
  private val groupExt = GroupExtension(system)
  private val valuesExt = ValuesExtension(system)
  private val apnsExt = ApplePushExtension(system)
  private val gcmExt = GooglePushExtension(system)
  private val actorPush = ActorPush(system)
  private val webrtcExt = WebrtcExtension(system)

  case class Device(
    deviceId:     EventBus.DeviceId,
    client:       EventBus.Client,
    peerSettings: Option[ApiPeerSettings],
    isJoined:     Boolean
  ) {
    def canPreConnect(pairDevice: Device): Boolean =
      (isJoined && pairDevice.isJoined) ||
        (peerSettings.flatMap(_.canPreConnect).contains(true) && pairDevice.peerSettings.flatMap(_.canPreConnect).contains(true))
  }

  object Pair {
    def build(d1: EventBus.DeviceId, d2: EventBus.DeviceId) = {
      if (d1 == d2) None
      else if (d1 < d2) Some(new Pair(d1, d2))
      else Some(new Pair(d2, d1))
    }

    def buildUnsafe(d1: EventBus.DeviceId, d2: EventBus.DeviceId) =
      build(d1, d2) getOrElse (throw new IllegalArgumentException(s"Attempt to pair with itself, deviceId: $d1"))
  }
  class Pair private (val left: EventBus.DeviceId, val right: EventBus.DeviceId) {
    override def equals(obj: Any) = Option(obj) match {
      case Some(pair: Pair) if pair.left == left && pair.right == right ⇒ true
      case _ ⇒ false
    }
    override def hashCode = s"${left}_${right}".hashCode
  }

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
        callees ← fetchMembers(callerUserId, peer) map (_ filterNot (_ == callerUserId))
        eventBusId ← eventBusExt.create(eventBusClient, timeout = None, isOwned = Some(true)) map (_._1)
        callerDeviceId ← eventBusExt.join(EventBus.ExternalClient(s.callerUserId, s.callerAuthId), eventBusId, if (s.timeout.nonEmpty) s.timeout else Some(10000))
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

      log.debug("Senfing smsg {} {}", smsg, memberUserIds)

      (for {
        _ ← if (peer.`type`.isPrivate) FutureExt.ftraverse(memberUserIds.toSeq)(userId ⇒ dialogExt.sendMessage(
          peer = ApiPeer(ApiPeerType.Private, (memberUserIds - userId).head),
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
      log.debug(s"Sending NeedOffer to ${device.deviceId} with ${pairDevice.deviceId}")
      eventBusExt.post(
        EventBus.InternalClient(self),
        eventBusId,
        Seq(device.deviceId),
        ApiNeedOffer(pairDevice.deviceId, sessionId, pairDevice.peerSettings).toByteArray
      )

      sessions += Pair.buildUnsafe(device.deviceId, pairDevice.deviceId) → sessionId
      sessionId
    }

    {
      case JoinCall(userId, authId) ⇒
        val client = EventBus.ExternalClient(userId, authId)

        (for {
          member ← getMember(userId)
          deviceId ← clients get client
          device ← devices get deviceId
        } yield member → device) match {
          case Some((member, device)) ⇒
            putDevice(device.deviceId, client, device.copy(isJoined = true))
            setMemberJoined(userId)
            cancelIncomingCallUpdates(userId)

            weakUpdExt.broadcastUserWeakUpdate(userId, UpdateCallHandled(id, Some(member.callAttempts)), excludeAuthIds = Set(authId))

            val connectedDevices =
              devices.view filterNot (_._1 == device.deviceId) map (_._2) filter (_.isJoined) map {
                case pairDevice ⇒
                  val sessionId =
                    sessions.getOrElse(Pair.buildUnsafe(device.deviceId, pairDevice.deviceId), connect(device, pairDevice))

                  log.debug("Sending EnableConnection to {} and {}", device.deviceId, pairDevice.deviceId)
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
        getMember(userId) match {
          case Some(member) ⇒
            cancelIncomingCallUpdates(userId)
            log.debug(s"member[userId=${userId}] rejected call")
            setMemberState(userId, MemberStates.Ended)
            val client = EventBus.ExternalClient(userId, authId)
            for (deviceId ← clients get client) {
              clients -= client
              devices -= deviceId
            }
            weakUpdExt.broadcastUserWeakUpdate(userId, UpdateCallHandled(id, Some(member.callAttempts)), excludeAuthIds = Set(authId))
            broadcastSyncedSet()
            sender() ! RejectCallAck

            if ( // If caller changed his mind until anyone picked up
            (!this.isConversationStarted && userId == callerUserId) ||
              // If everyone rejected dialing, there will no any conversation ;(
              (!this.isConversationStarted && everyoneRejected(callerUserId))) end()
          case None ⇒ throw new IllegalStateException("Attempted to reject call of a non-existent member")
        }
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
            log.debug("AdvertiseSelf {}", msg)
            for (deviceId ← ebMessage.deviceId) yield {
              val newDevice = Device(deviceId, ebMessage.client, msg.peerSettings, isJoined = deviceId == callerDeviceId)
              log.debug(s"newDevice ${newDevice.deviceId} ${newDevice.peerSettings}")
              devices.values.view filterNot (_.deviceId == newDevice.deviceId) foreach { pairDevice ⇒
                if (pairDevice.canPreConnect(newDevice)) {
                  log.debug(s"canPreConnect is true for device ${pairDevice.deviceId} ${pairDevice.peerSettings}")
                  connect(newDevice, pairDevice)
                }
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
              if (deviceId != msg.device) {
                val chkPair = Pair.buildUnsafe(deviceId, msg.device)
                if (pair.left == chkPair.left && pair.right == chkPair.right) {
                  sessions = sessions filterNot (_ == sessionId)
                  eventBusExt.post(EventBus.InternalClient(self), eventBusId, Seq(pair.left), ApiCloseSession(pair.right, sessionId).toByteArray)
                  eventBusExt.post(EventBus.InternalClient(self), eventBusId, Seq(pair.right), ApiCloseSession(pair.left, sessionId).toByteArray)
                  connect(leftDevice, rightDevice)
                } else log.warning("Received OnRenegotiationNeeded for a wrong deviceId")
              }
            }
          case _ ⇒
        }
      case EventBus.Disconnected(_, client, deviceId) ⇒
        removeDevice(deviceId)
        client.externalUserId foreach { userId ⇒
          if (!devices.values.exists(_.client.externalUserId.contains(userId))) {
            log.debug(s"member[userId=${userId}] disconnected from eventbus")
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
        getMember(userId) match {
          case Some(member) ⇒
            if (System.currentTimeMillis() - startTime < 30000) {
              weakUpdExt.broadcastUserWeakUpdate(
                userId,
                UpdateIncomingCall(id, Some(member.callAttempts)),
                reduceKey = Some(s"call_${id}_${member.callAttempts}")
              )
            } else {
              log.debug(s"Auto-rejecting member[userId=$userId] due to timeout")
              cancelIncomingCallUpdates(userId)
              weakUpdExt.broadcastUserWeakUpdate(
                userId,
                UpdateCallHandled(id, Some(member.callAttempts))
              )
              setMemberState(userId, MemberStates.Ended)
              for {
                deviceId ← clients.filter(_._1.externalUserId.contains(userId)).values
              } yield removeDevice(deviceId)
              broadcastSyncedSet()
            }
          case None ⇒ throw new IllegalStateException("Attempted to send incoming call update of a non-existent member")
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

  private def fetchMembers(callerUserId: Int, peer: Peer): Future[Seq[Int]] =
    peer match {
      case Peer(PeerType.Private, userId) ⇒
        withNonBlockedUser(callerUserId, userId)(
          default = FastFuture.successful(Seq(callerUserId, userId)),
          failed = FastFuture.failed(WebrtcCallErrors.CallForbidden)
        )
      case Peer(PeerType.Group, groupId) ⇒
        groupExt.getMemberIds(groupId) flatMap {
          case (memberIds, _, _) if memberIds.length <= 25 ⇒ FastFuture.successful(memberIds)
          case _ ⇒ FastFuture.failed(WebrtcCallErrors.GroupTooBig)
        }
      case _ ⇒ FastFuture.failed(new RuntimeException(s"Unknown peer type: ${peer.`type`}"))
    }

  private def scheduleIncomingCallUpdates(callees: Seq[UserId]): Future[Unit] = {
    val pushCredsFu = for {
      authIdsMap ← userExt.getAuthIdsMap(callees.toSet)
      acredsMap ← FutureExt.ftraverse(authIdsMap.toSeq) {
        case (userId, authIds) ⇒
          apnsExt.fetchVoipCreds(authIds.toSet) map (userId → _)
      }
      gcredsMap ← FutureExt.ftraverse(authIdsMap.toSeq) {
        case (userId, authIds) ⇒
          gcmExt.fetchCreds(authIds.toSet) map (userId → _)
      }
      actorCredsMap ← FutureExt.ftraverse(authIdsMap.toSeq) {
        case (userId, authIds) ⇒
          actorPush.fetchCreds(userId) map (userId → _)
      }
    } yield (acredsMap, gcredsMap, actorCredsMap)

    pushCredsFu map {
      case (appleCreds, googleCreds, actorCreds) ⇒
        for {
          (userId, credsList) ← appleCreds
          creds ← credsList
          credsId = extractCredsId(creds)
          clientFu ← apnsExt.voipClient(credsId)
          payload = (new ApnsPayloadBuilder)
            .addCustomProperty("callId", id)
            .addCustomProperty("attemptIndex", 1)
            .buildWithDefaultMaximumLength()
          _ = clientFu foreach { implicit c ⇒ sendNotification(payload, creds, userId) }
        } yield ()

        for {
          (member, creds) ← googleCreds
          cred ← creds
          message = new GooglePushMessage(
            cred.regId,
            None,
            Some(Map("callId" → id.toString, "attemptIndex" → "1")),
            time_to_live = Some(0)
          )
          _ = gcmExt.send(cred.projectId, message)
        } yield ()

        for {
          (member, creds) ← actorCreds
          cred ← creds
          _ = actorPush.deliver(ActorPushMessage(
            "callId" → id.toString,
            "attemptIndex" → "1"
          ), cred)
        } yield ()

        scheduledUpds = (callees map { userId ⇒
          userId → system.scheduler.schedule(0.seconds, 5.seconds, self, SendIncomingCall(userId))
        }).toMap
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