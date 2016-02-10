package im.actor.server.webrtc

import akka.actor._
import akka.pattern.pipe
import im.actor.api.rpc.messaging.{ ApiServiceMessage, ApiServiceExPhoneCall }
import im.actor.api.rpc.peers.{ ApiPeerType, ApiPeer }
import im.actor.api.rpc.webrtc._
import im.actor.concurrent.ActorStashing
import im.actor.server.db.DbExtension
import im.actor.server.dialog.DialogExtension
import im.actor.server.eventbus.{ EventBus, EventBusExtension }
import im.actor.server.persist.webrtc.WebrtcCallRepo
import im.actor.server.sequence.WeakUpdatesExtension
import im.actor.types._

import scala.concurrent.forkjoin.ThreadLocalRandom

sealed abstract class WebrtcCallError(message: String) extends RuntimeException(message)

object WebrtcCallErrors {
  object NotAParticipant extends WebrtcCallError("Not participant")
  object CallNotStarted extends WebrtcCallError("Call not started")
  object CallAlreadyStarted extends WebrtcCallError("Call already started")
}

sealed trait WebrtcCallMessage

object WebrtcCallMessages {
  final case class StartCall(callerUserId: Int, receiverUserId: Int, eventBusId: String) extends WebrtcCallMessage
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
  private val db = DbExtension(context.system).db

  def receive = waitForStart

  def waitForStart: Receive = {
    case StartCall(callerUserId, receiverUserId, eventBusId) ⇒
      val update = UpdateIncomingCall(id)

      (for {
        _ ← db.run(WebrtcCallRepo.create(WebrtcCall(id, callerUserId, receiverUserId)))
        _ ← weakUpdExt.broadcastUserWeakUpdate(receiverUserId, update, None, Some(Webrtc.WeakGroup))
        _ ← eventBusExt.subscribe(eventBusId, self)
      } yield ()) pipeTo self

      becomeStashing(replyTo ⇒ {
        case () ⇒
          replyTo ! StartCallAck
          context become callInProgress(eventBusId, System.currentTimeMillis(), callerUserId, receiverUserId)
          unstashAll()
        case failure: Status.Failure ⇒
          replyTo forward failure
          throw failure.cause
      }, discardOld = true)
    case _ ⇒ sender() ! Status.Failure(WebrtcCallErrors.CallNotStarted)
  }

  def callInProgress(
    eventBusId:     String,
    startTime:      Long,
    callerUserId:   Int,
    receiverUserId: Int
  ): Receive = {
    def end(): Unit = {
      val duration = ((System.currentTimeMillis() - startTime) / 1000).toInt

      (for {
        _ ← dialogExt.sendMessage(
          peer = ApiPeer(ApiPeerType.Private, receiverUserId),
          senderUserId = callerUserId,
          senderAuthId = None,
          senderAuthSid = 0,
          randomId = ThreadLocalRandom.current().nextLong,
          message = ApiServiceMessage("Call ended", Some(ApiServiceExPhoneCall(duration)))
        )
        _ ← db.run(WebrtcCallRepo.delete(id))
      } yield PoisonPill) pipeTo self onFailure {
        case e ⇒
          log.error(e, "Failed to stop call")
          context stop self
      }
    }

    def withOrigin(origin: Int)(f: Int ⇒ Any) =
      if (callerUserId == origin)
        f(receiverUserId)
      else if (receiverUserId == origin)
        f(callerUserId)
      else
        sender() ! Status.Failure(WebrtcCallErrors.NotAParticipant)

    {
      case EventBus.Disposed(_) ⇒ end()
      case EventBus.Message(_, userId, message) ⇒
        ApiWebRTCSignaling.parseFrom(message).right foreach {
          case ApiAnswerCall ⇒
            context become callInProgress(eventBusId, System.currentTimeMillis(), callerUserId, receiverUserId)
          case ApiEndCall ⇒
            withOrigin(userId)(_ ⇒ end())
          case _ ⇒
        }
      case GetInfo ⇒
        sender() ! GetInfoAck(eventBusId, callerUserId, Seq(callerUserId, receiverUserId))
      case _: StartCall ⇒ sender() ! WebrtcCallErrors.CallAlreadyStarted
    }
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    log.error(reason, "Failure on message: {}", message)
  }
}