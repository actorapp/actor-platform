package im.actor.server.activation.common

import akka.actor.{ Actor, ActorLogging, Props }
import akka.pattern.pipe
import cats.data.Xor

import scala.concurrent.Future
import scala.concurrent.duration._

object ActivationStateActor {

  private[activation] final case class Send(code: Code)

  private[activation] case class SendAck(result: CodeFailure Xor Unit)

  private[activation] final case class ForgetSentCode(code: Code)

  def props[Id, CodeType <: Code](repeatLimit: Duration, sendAction: CodeType ⇒ Future[Unit], id: CodeType ⇒ Id) =
    Props(new ActivationStateActor(repeatLimit, sendAction, id))

}

class ActivationStateActor[Id, CodeType <: Code](repeatLimit: Duration, send: CodeType ⇒ Future[Unit], codeId: CodeType ⇒ Id) extends Actor with ActorLogging {
  implicit val system = context.system
  implicit val ec = context.dispatcher

  import ActivationStateActor._

  private val sentCodes = new scala.collection.mutable.HashSet[Id]()

  def codeWasNotSent(code: CodeType) = !sentCodes.contains(codeId(code))

  def rememberSentCode(code: CodeType) = sentCodes += codeId(code)

  def forgetSentCode(code: CodeType) = sentCodes -= codeId(code)

  def forgetSentCodeAfterDelay(code: CodeType) =
    system.scheduler.scheduleOnce(repeatLimit.toMillis.millis, self, ForgetSentCode(code))

  override def receive: Receive = {
    case Send(code: CodeType @unchecked) ⇒
      (sendCode(code) map SendAck) pipeTo sender()
    case ForgetSentCode(code: CodeType @unchecked) ⇒ forgetSentCode(code)
  }

  private def sendCode(code: CodeType): Future[CodeFailure Xor Unit] = {
    if (codeWasNotSent(code)) {
      log.debug(s"Sending $code")

      rememberSentCode(code)

      send(code) map { _ ⇒
        forgetSentCodeAfterDelay(code)
        Xor.right(())
      } recover {
        case e ⇒
          log.error(e, "Failed to send code: {}", code)
          Xor.left(SendFailure("Unable to send code"))
      }
    } else {
      log.debug(s"Ignoring send $code")
      Future.successful(Xor.left(BadRequest("Try to request code later")))
    }
  }

}
