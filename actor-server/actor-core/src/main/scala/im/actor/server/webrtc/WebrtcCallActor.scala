package im.actor.server.webrtc

import akka.actor._
import akka.pattern.pipe
import im.actor.api.rpc.messaging.{ ApiServiceMessage, ApiServiceExPhoneCall }
import im.actor.api.rpc.peers.{ ApiPeerType, ApiPeer }
import im.actor.api.rpc.webrtc._
import im.actor.concurrent.ActorStashing
import im.actor.server.db.DbExtension
import im.actor.server.dialog.DialogExtension
import im.actor.server.persist.webrtc.WebrtcCallRepo
import im.actor.server.sequence.{ WeakUpdatesExtension, SeqUpdatesExtension }

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom

sealed abstract class WebrtcCallError(message: String) extends RuntimeException(message)

object WebrtcCallErrors {
  object NotAParticipant extends WebrtcCallError("Not participant")
}

sealed trait WebrtcCallMessages

object WebrtcCallMessages {
  final case class StartCall(callerUserId: Int, receiverUserId: Int) extends WebrtcCallMessages
  case object CallStarted

  final case class CallInProgress(userId: Int, timeout: Int) extends WebrtcCallMessages

  final case class CallSignal(userId: Int, pkg: Array[Byte]) extends WebrtcCallMessages

  case class EndCall(userId: Int) extends WebrtcCallMessages
}

final case class WebrtcCallEnvelope(id: Long, message: WebrtcCallMessages)

object WebrtcCallActor {
  val RegionTypeName = "WebrtcCall"
  val DefaultCallTimeout = 30.seconds

  def props = Props(classOf[WebrtcCallActor])
}

private final class WebrtcCallActor extends ActorStashing with ActorLogging {
  import WebrtcCallMessages._
  import WebrtcCallActor._
  import context.dispatcher

  private val id = self.path.name.toLong

  private lazy val weakUpdExt = WeakUpdatesExtension(context.system)
  private lazy val dialogExt = DialogExtension(context.system)
  private val db = DbExtension(context.system).db

  def receive = waitForStart

  def waitForStart: Receive = {
    case StartCall(callerUserId, receiverUserId) ⇒
      val update = UpdateIncomingCall(id, callerUserId)

      (for {
        _ ← db.run(WebrtcCallRepo.create(WebrtcCall(id, callerUserId, receiverUserId)))
        _ ← weakUpdExt.broadcastUserWeakUpdate(receiverUserId, update, None, Some(Webrtc.WeakGroup))
      } yield ()) pipeTo self

      becomeStashing(replyTo ⇒ {
        case () ⇒
          replyTo ! CallStarted
          context become callInProgress(System.currentTimeMillis(), callerUserId, receiverUserId, scheduleEnd(DefaultCallTimeout))
          unstashAll()
        case failure: Status.Failure ⇒
          replyTo forward failure
          throw failure.cause
      }, discardOld = true)
  }

  def callInProgress(startTime: Long, callerUserId: Int, receiverUserId: Int, scheduledEnd: Cancellable): Receive = {
    def end(): Future[Unit] = {
      val duration = ((System.currentTimeMillis() - startTime) / 1000).toInt
      val update = UpdateCallEnded(id)

      for {
        _ ← weakUpdExt.broadcastUserWeakUpdate(callerUserId, update, None)
        _ ← weakUpdExt.broadcastUserWeakUpdate(receiverUserId, update, None)
        _ ← dialogExt.sendMessage(
          peer = ApiPeer(ApiPeerType.Private, receiverUserId),
          senderUserId = callerUserId,
          senderAuthSid = 0,
          randomId = ThreadLocalRandom.current().nextLong,
          message = ApiServiceMessage("Call ended", Some(ApiServiceExPhoneCall(duration)))
        )
        _ ← db.run(WebrtcCallRepo.delete(id))
      } yield ()
    }

    def withOrigin(origin: Int)(f: Int ⇒ Any) =
      if (callerUserId == origin)
        f(receiverUserId)
      else if (receiverUserId == origin)
        f(callerUserId)
      else
        sender() ! Status.Failure(WebrtcCallErrors.NotAParticipant)

    {
      case CallInProgress(userId, timeout) ⇒
        withOrigin(userId) { _ ⇒
          scheduledEnd.cancel()
          scheduleEnd(timeout.seconds)
          val update = UpdateCallInProgress(id, timeout)

          (for {
            _ ← weakUpdExt.broadcastUserWeakUpdate(receiverUserId, update, Some(s"webrtc_call_inprogress_$id"), Some(Webrtc.WeakGroup))
            _ ← weakUpdExt.broadcastUserWeakUpdate(callerUserId, update, Some(s"webrtc_call_inprogress_$id"), Some(Webrtc.WeakGroup))
          } yield ()) pipeTo self

          becomeStashing(replyTo ⇒ {
            case () ⇒
              context.unbecome()
              unstashAll()
            case failure: Status.Failure ⇒
              end()
              throw failure.cause
          }, discardOld = false)
        }
      case CallSignal(userId, pkg) ⇒
        withOrigin(userId) { target ⇒
          // TODO: stashing
          val update = UpdateCallSignal(id, pkg)
          weakUpdExt.broadcastUserWeakUpdate(target, update, Some(s"webrtc_call_signal_$id"), Some(Webrtc.WeakGroup))
        }
      case EndCall(userId) ⇒
        withOrigin(userId) { _ ⇒
          scheduledEnd.cancel()

          end() map (_ ⇒ PoisonPill) pipeTo self onFailure {
            case e ⇒ log.error(e, "Failed to end call")
          }
        }
    }
  }

  def scheduleEnd(timeout: FiniteDuration): Cancellable = context.system.scheduler.scheduleOnce(timeout, self, EndCall)
}