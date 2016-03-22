package im.actor.server.activation.common

import akka.actor.{ Actor, ActorLogging, Props }
import akka.pattern.pipe
import cats.data.Xor

import scala.concurrent.Future
import scala.concurrent.duration._

object ActivationStateActor {

  private[activation] final case class Send(code: Code)

  private[activation] case class SendAck(result: CodeFailure Xor Unit)

  object ForgetSentCode {
    def phone(phone: Long) = ForgetSentCode(phone)
    def email(email: String) = ForgetSentCode(email)
  }
  private[activation] final case class ForgetSentCode[T](codeId: T)

  def props[Id, CodeType <: Code](repeatLimit: Duration, sendAction: CodeType ⇒ Future[Unit], id: CodeType ⇒ Id) =
    Props(new ActivationStateActor(repeatLimit, sendAction, id))

}

private[activation] final class ActivationStateActor[Id, CodeType <: Code](repeatLimit: Duration, send: CodeType ⇒ Future[Unit], extractId: CodeType ⇒ Id) extends Actor with ActorLogging {
  implicit val system = context.system
  implicit val ec = context.dispatcher

  import ActivationStateActor._

  private val sentCodes = new scala.collection.mutable.HashSet[Id]()

  def codeWasNotSent(code: CodeType) = !sentCodes.contains(extractId(code))

  def rememberSentCode(code: CodeType) = sentCodes += extractId(code)

  def forgetSentCode(codeId: Id) = sentCodes -= codeId

  def forgetSentCodeAfterDelay(code: CodeType) =
    system.scheduler.scheduleOnce(repeatLimit.toMillis.millis, self, ForgetSentCode(extractId(code)))

  override def receive: Receive = {
    case Send(code: CodeType @unchecked)       ⇒ (sendCode(code) map SendAck) pipeTo sender()
    case ForgetSentCode(codeId: Id @unchecked) ⇒ forgetSentCode(codeId)
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
      Future.successful(Xor.left(BadRequest("Too frequent code requests")))
    }
  }

}
