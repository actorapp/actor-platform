package im.actor.server.activation.internal

import java.time.{ ZoneOffset, LocalDateTime }
import java.time.temporal.ChronoUnit._

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._
import scalaz.{ \/-, \/ }

import akka.actor._
import akka.stream.Materializer

import im.actor.server.activation.Activation.{ Code, EmailCode, SmsCode }
import im.actor.server.activation._
import im.actor.server.activation.internal.InternalCodeActivation.Send
import im.actor.server.email.{ EmailSender, Message }
import im.actor.server.models.AuthCode
import im.actor.server.persist
import im.actor.server.sms.AuthSmsEngine
import slick.driver.PostgresDriver.api._

object InternalCodeActivation {
  private[activation] sealed trait Message
  private[activation] final case class Send(code: Code)
  private[activation] final case class ForgetSentCode(code: Code) extends Message

  def newContext(config: ActivationConfig, smsEngine: AuthSmsEngine, emailSender: EmailSender)(
    implicit
    system:       ActorSystem,
    materializer: Materializer,
    db:           Database,
    ec:           ExecutionContext
  ): InternalCodeActivation = {
    new InternalCodeActivation(
      system.actorOf(Props(classOf[Activation], config.repeatLimit, smsEngine, emailSender, materializer), "activation"),
      config
    )
  }
}

private[activation] class InternalCodeActivation(activationActor: ActorRef, config: ActivationConfig)(implicit db: Database, ec: ExecutionContext) extends CodeActivation {
  import im.actor.server.activation.Activation._

  def send(transactionHash: Option[String], code: Code): DBIO[String \/ Unit] = transactionHash match {
    case Some(hash) ⇒
      for {
        _ ← persist.AuthCode.create(hash, code.code)
        result ← DBIO.from(send(code))
      } yield result
    case None ⇒ DBIO.successful(\/-(send(code)))
  }

  def validate(transactionHash: String, code: String): DBIO[ValidationResponse] =
    for {
      optCode ← persist.AuthCode.findByTransactionHash(transactionHash)
      result ← optCode map {
        case s if isExpired(s) ⇒
          for (_ ← persist.AuthCode.deleteByTransactionHash(transactionHash)) yield ExpiredCode
        case s if s.code != code ⇒
          if (s.attempts + 1 >= config.attempts) {
            for (_ ← persist.AuthCode.deleteByTransactionHash(transactionHash)) yield ExpiredCode
          } else {
            for (_ ← persist.AuthCode.incrementAttempts(transactionHash, s.attempts)) yield InvalidCode
          }
        case _ ⇒ DBIO.successful(Validated)
      } getOrElse DBIO.successful(InvalidHash)
    } yield result

  def finish(transactionHash: String): DBIO[Unit] = persist.AuthCode.deleteByTransactionHash(transactionHash).map(_ ⇒ ())

  private def isExpired(code: AuthCode): Boolean =
    code.createdAt.plus(config.expiration.toMillis, MILLIS).isBefore(LocalDateTime.now(ZoneOffset.UTC))

  private def send(code: Code): Future[String \/ Unit] = {
    code match {
      case SmsCode(phone, _) ⇒ if (!phone.toString.startsWith("7555")) activationActor ! Send(code)
      case _: EmailCode      ⇒ activationActor ! Send(code)
    }
    Future.successful(\/-(()))
  }
}

class Activation(repeatLimit: Duration, smsEngine: AuthSmsEngine, emailSender: EmailSender)(implicit materializer: Materializer) extends Actor with ActorLogging {
  import InternalCodeActivation._

  implicit val system = context.system
  implicit val ec = context.dispatcher

  private val sentCodes = new scala.collection.mutable.HashSet[Code]()

  def codeWasNotSent(code: Code) = !sentCodes.contains(code)
  def rememberSentCode(code: Code) = sentCodes += code
  def forgetSentCode(code: Code) = sentCodes -= code
  def forgetSentCodeAfterDelay(code: Code) =
    system.scheduler.scheduleOnce(repeatLimit.toMillis.millis, self, ForgetSentCode(code))

  override def receive: Receive = {
    case Send(code)           ⇒ sendCode(code)
    case ForgetSentCode(code) ⇒ forgetSentCode(code)
  }

  private def sendCode(code: Code): Unit = {
    if (codeWasNotSent(code)) {
      log.debug(s"Sending $code")

      rememberSentCode(code)

      code match {
        case SmsCode(phone, c)   ⇒ smsEngine.sendCode(phone, c)
        case EmailCode(email, c) ⇒ emailSender.send(Message(email, "Actor activation code", s"$c is your Actor code"))
      }

      forgetSentCodeAfterDelay(code)
    } else {
      log.debug(s"Ignoring send $code")
    }
  }

}
