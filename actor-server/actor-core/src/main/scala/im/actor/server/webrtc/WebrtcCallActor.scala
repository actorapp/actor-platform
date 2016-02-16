package im.actor.server.webrtc

import akka.actor._
import akka.http.scaladsl.util.FastFuture
import akka.pattern.pipe
import im.actor.api.rpc.messaging.{ ApiServiceMessage, ApiServiceExPhoneCall }
import im.actor.api.rpc.peers.{ ApiPeerType, ApiPeer }
import im.actor.api.rpc.webrtc._
import im.actor.concurrent.{ FutureExt, ActorStashing }
import im.actor.server.dialog.DialogExtension
import im.actor.server.eventbus.{ EventBus, EventBusExtension }
import im.actor.server.group.GroupExtension
import im.actor.server.model.{ PeerType, Peer }
import im.actor.server.sequence.WeakUpdatesExtension
import im.actor.types._

import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom

sealed abstract class WebrtcCallError(message: String) extends RuntimeException(message)

object WebrtcCallErrors {
  object NotAParticipant extends WebrtcCallError("Not participant")
  object CallNotStarted extends WebrtcCallError("Call not started")
  object CallAlreadyStarted extends WebrtcCallError("Call already started")
}

sealed trait WebrtcCallMessage

object WebrtcCallMessages {
  final case class StartCall(callerUserId: Int, peer: Peer, eventBusId: String) extends WebrtcCallMessage
  case object StartCallAck

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

  private var scheduledUpd: Option[Cancellable] = None

  def receive = waitForStart

  def waitForStart: Receive = {
    case StartCall(callerUserId, peer, eventBusId) ⇒
      case class Res(callees: Seq[Int], schedUpd: Cancellable)

      val update = UpdateIncomingCall(id)

      (for {
        callees ← fetchParticipants(callerUserId, peer) map (_ filterNot (_ == callerUserId))
        _ ← eventBusExt.subscribe(eventBusId, self)
      } yield Res(callees, context.system.scheduler.schedule(0.seconds, 5.seconds) {
        weakUpdExt.broadcastUsersWeakUpdate(callees, update, None, None)
      })) pipeTo self

      becomeStashing(replyTo ⇒ {
        case Res(callees, schedUpd) ⇒
          this.scheduledUpd = Some(schedUpd)
          replyTo ! StartCallAck
          context become callInProgress(eventBusId, System.currentTimeMillis(), callerUserId, callees :+ callerUserId)
          unstashAll()
        case failure: Status.Failure ⇒
          replyTo forward failure
          context stop self
      }, discardOld = true)
    case _ ⇒ sender() ! Status.Failure(WebrtcCallErrors.CallNotStarted)
  }

  def callInProgress(
    eventBusId:   String,
    startTime:    Long,
    callerUserId: Int,
    participants: Seq[Int]
  ): Receive = {
    def end(): Unit = {
      val duration = ((System.currentTimeMillis() - startTime) / 1000).toInt

      (for {
        _ ← FutureExt.ftraverse(participants)(userId ⇒ dialogExt.sendMessage(
          peer = ApiPeer(ApiPeerType.Private, userId),
          senderUserId = callerUserId,
          senderAuthId = None,
          senderAuthSid = 0,
          randomId = ThreadLocalRandom.current().nextLong,
          message = ApiServiceMessage("Call ended", Some(ApiServiceExPhoneCall(duration)))
        ))
      } yield PoisonPill) pipeTo self onFailure {
        case e ⇒
          log.error(e, "Failed to stop call")
          context stop self
      }
    }

    {
      case EventBus.Disposed(_) ⇒ end()
      case EventBus.Message(_, userId, message) ⇒
        ApiWebRTCSignaling.parseFrom(message).right foreach {
          case ApiAnswerCall ⇒
            scheduledUpd foreach (_.cancel())
            weakUpdExt.broadcastUsersWeakUpdate(participants.filterNot(_ == userId), UpdateCallHandled(id))
            context become callInProgress(eventBusId, System.currentTimeMillis(), callerUserId, participants)
          case ApiEndCall ⇒
            scheduledUpd foreach (_.cancel())
            end()
          case _ ⇒
        }
      case GetInfo ⇒
        sender() ! GetInfoAck(eventBusId, callerUserId, participants)
      case _: StartCall ⇒ sender() ! WebrtcCallErrors.CallAlreadyStarted
    }
  }

  private def fetchParticipants(callerUserId: Int, peer: Peer) =
    peer match {
      case Peer(PeerType.Private, userId) ⇒ FastFuture.successful(Seq(callerUserId, userId))
      case Peer(PeerType.Group, groupId)  ⇒ groupExt.getMemberIds(groupId) map (_._1)
      case _                              ⇒ FastFuture.failed(new RuntimeException(s"Unknown peer type: ${peer.`type`}"))
    }

  override def postStop(): Unit = {
    scheduledUpd foreach (_.cancel())
    super.postStop()
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    log.error(reason, "Failure on message: {}", message)
  }
}