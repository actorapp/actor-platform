package im.actor.server.activation.internal

import java.time.temporal.ChronoUnit._
import java.time.{ LocalDateTime, ZoneOffset }

import akka.actor._
import akka.pattern.ask
import akka.pattern.pipe
import akka.stream.Materializer
import akka.util.Timeout
import im.actor.server.activation.Activation.{ CallCode, Code, EmailCode, SmsCode }
import im.actor.server.activation._
import im.actor.server.email.{ EmailSender, Content, Message }
import im.actor.server.model.AuthCode
import im.actor.server.persist
import im.actor.server.sms.{ AuthCallEngine, AuthSmsEngine }
import im.actor.util.misc.EmailUtils.isTestEmail
import im.actor.util.misc.PhoneNumberUtils.isTestPhone
import slick.driver.PostgresDriver.api._

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scalaz.{ -\/, \/, \/- }

object InternalCodeActivation {

  private[activation] sealed trait Message

  private[activation] final case class Send(code: Code)

  private[activation] case class SendAck(result: CodeFailure \/ Unit)

  private[activation] final case class ForgetSentCode(code: Code) extends Message

  def newContext(config: ActivationConfig, smsEngine: AuthSmsEngine, callEngine: AuthCallEngine, emailSender: EmailSender)(
    implicit
    system:       ActorSystem,
    materializer: Materializer,
    db:           Database,
    ec:           ExecutionContext
  ): InternalCodeActivation = {
    new InternalCodeActivation(
      system.actorOf(Props(classOf[Activation], config.repeatLimit, smsEngine, callEngine, emailSender, materializer), "activation"),
      config
    )
  }

  def validateAction(txHash: String, code: String, attemptsNum: Int, expiration: Long)(implicit ec: ExecutionContext): DBIO[ValidationResponse] =
    for {
      optCode ← persist.AuthCodeRepo.findByTransactionHash(txHash)
      result ← optCode map {
        case s if isExpired(s, expiration) ⇒
          for (_ ← persist.AuthCodeRepo.deleteByTransactionHash(txHash)) yield ExpiredCode
        case s if s.code != code ⇒
          if (s.attempts + 1 >= attemptsNum) {
            for (_ ← persist.AuthCodeRepo.deleteByTransactionHash(txHash)) yield ExpiredCode
          } else {
            for (_ ← persist.AuthCodeRepo.incrementAttempts(txHash, s.attempts)) yield InvalidCode
          }
        case _ ⇒ DBIO.successful(Validated)
      } getOrElse DBIO.successful(InvalidHash)
    } yield result

  def finishAction(txHash: String)(implicit ec: ExecutionContext): DBIO[Unit] = persist.AuthCodeRepo.deleteByTransactionHash(txHash).map(_ ⇒ ())

  def isExpired(code: AuthCode, expiration: Long): Boolean =
    code.createdAt.plus(expiration, MILLIS).isBefore(LocalDateTime.now(ZoneOffset.UTC))
}

private[activation] final class InternalCodeActivation(activationActor: ActorRef, config: ActivationConfig)(implicit db: Database, ec: ExecutionContext) extends CodeActivation {

  import InternalCodeActivation._
  import im.actor.server.activation.Activation._

  implicit val timeout: Timeout = Timeout(20.seconds)

  def send(transactionHash: Option[String], code: Code): DBIO[CodeFailure \/ Unit] = (transactionHash match {
    case Some(hash) ⇒ for (_ ← persist.AuthCodeRepo.createOrUpdate(hash, code.code)) yield ()
    case None       ⇒ DBIO.successful(())
  }) flatMap (_ ⇒ DBIO.from(sendCode(code)))

  def validate(txHash: String, code: String): DBIO[ValidationResponse] = validateAction(txHash, code, config.attempts, config.expiration.toMillis)

  def finish(txHash: String): DBIO[Unit] = finishAction(txHash)

  private def sendCode(code: Code): Future[CodeFailure \/ Unit] = code match {
    case p: PhoneCode if isTestPhone(p.phone) ⇒ Future.successful(\/-(()))
    case m: EmailCode if isTestEmail(m.email) ⇒ Future.successful(\/-(()))
    case _                                    ⇒ (activationActor ? Send(code)).mapTo[SendAck].map(_.result)
  }

}

class Activation(repeatLimit: Duration, smsEngine: AuthSmsEngine, callEngine: AuthCallEngine, emailSender: EmailSender)(implicit materializer: Materializer) extends Actor with ActorLogging {

  import InternalCodeActivation._

  implicit val system = context.system
  implicit val ec = context.dispatcher

  private val sentCodes = new scala.collection.mutable.HashSet[Code]()

  private val emailTemplate = EmailTemplate.template

  def codeWasNotSent(code: Code) = !sentCodes.contains(code)

  def rememberSentCode(code: Code) = sentCodes += code

  def forgetSentCode(code: Code) = sentCodes -= code

  def forgetSentCodeAfterDelay(code: Code) =
    system.scheduler.scheduleOnce(repeatLimit.toMillis.millis, self, ForgetSentCode(code))

  override def receive: Receive = {
    case Send(code) ⇒
      (sendCode(code) map SendAck) pipeTo sender()
    case ForgetSentCode(code) ⇒ forgetSentCode(code)
  }

  private def sendCode(code: Code): Future[CodeFailure \/ Unit] = {
    if (codeWasNotSent(code)) {
      log.debug(s"Sending $code")

      rememberSentCode(code)

      (code match {
        case SmsCode(phone, c)            ⇒ smsEngine.sendCode(phone, c)
        case CallCode(phone, c, language) ⇒ callEngine.sendCode(phone, c, language)
        case EmailCode(email, c) ⇒
          emailSender.send(Message(email, s"Actor activation code: $c", Content(Some(emailTemplate.replace("$$CODE$$", c)), Some(s"Your actor activation code: $c"))))
      }) map { _ ⇒
        forgetSentCodeAfterDelay(code)
        \/-(())
      } recover {
        case e ⇒
          log.error(e, "Failed to send code: {}", code)
          -\/(SendFailure("Unable to send code"))
      }
    } else {
      log.debug(s"Ignoring send $code")
      Future.successful(-\/(BadRequest("Try to request code later")))
    }
  }

}
