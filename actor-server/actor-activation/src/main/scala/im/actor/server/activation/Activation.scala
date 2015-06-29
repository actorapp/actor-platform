package im.actor.server.activation

import scala.concurrent.duration.FiniteDuration

import akka.actor._
import akka.stream.Materializer

import im.actor.server.email.{ EmailSender, Message }
import im.actor.server.sms.AuthSmsEngine

object Activation {
  sealed trait Code
  case class SmsCode(phone: Long, code: String) extends Code
  case class EmailCode(email: String, code: String) extends Code

  private[activation] sealed trait Message
  private[activation] case class Send(authId: Long, code: Code)
  private[activation] case class ForgetSentCode(code: Code) extends Message

  def newContext(config: ActivationConfig, smsEngine: AuthSmsEngine, emailSender: EmailSender)(implicit system: ActorSystem, materializer: Materializer): ActivationContextImpl = {
    ActivationContextImpl(
      system.actorOf(Props(classOf[Activation], config.waitInterval, smsEngine, emailSender, materializer), "activation")
    )
  }
}

case class ActivationContextImpl(activationActor: ActorRef) extends ActivationContext {
  import Activation._

  def send(authId: Long, code: Code): Unit = {
    activationActor ! Send(authId, code)
  }
}

class Activation(waitInterval: FiniteDuration, smsEngine: AuthSmsEngine, emailSender: EmailSender)(implicit materializer: Materializer) extends Actor with ActorLogging {
  import Activation._

  implicit val system = context.system
  implicit val ec = context.dispatcher

  private val sentCodes = new scala.collection.mutable.HashSet[Code]()

  def codeWasNotSent(code: Code) = !sentCodes.contains(code)
  def rememberSentCode(code: Code) = sentCodes += code
  def forgetSentCode(code: Code) = sentCodes -= code
  def forgetSentCodeAfterDelay(code: Code) =
    system.scheduler.scheduleOnce(waitInterval, self, ForgetSentCode(code))

  override def receive: Receive = {
    case Send(authId, code)   ⇒ sendCode(authId, code)
    case ForgetSentCode(code) ⇒ forgetSentCode(code)
  }

  private def sendCode(authId: Long, code: Code): Unit = {
    if (codeWasNotSent(code)) {
      log.debug(s"Sending $code")

      rememberSentCode(code)

      code match {
        case SmsCode(phone, c)   ⇒ smsEngine.sendCode(phone, c)
        case EmailCode(email, c) ⇒ emailSender.send(Message(email, "Actor activation code", s"$code is your Actor code"))
      }

      forgetSentCodeAfterDelay(code)
    } else {
      log.debug(s"Ignoring send $code")
    }
  }

}
