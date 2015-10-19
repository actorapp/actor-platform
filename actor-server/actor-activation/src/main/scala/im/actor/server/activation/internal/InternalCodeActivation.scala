package im.actor.server.activation.internal

import java.time.temporal.ChronoUnit._
import java.time.{ LocalDateTime, ZoneOffset }

import akka.actor._
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import im.actor.server.activation.Activation.{ CallCode, Code, EmailCode, SmsCode }
import im.actor.server.activation._
import im.actor.server.email.{ EmailSender, Message }
import im.actor.server.models.AuthCode
import im.actor.server.persist
import im.actor.server.sms.{ AuthCallEngine, AuthSmsEngine }
import slick.driver.PostgresDriver.api._

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scalaz.{ -\/, \/, \/- }

object InternalCodeActivation {

  private[activation] sealed trait Message

  private[activation] final case class Send(code: Code)

  private[activation] case class SendAck(result: String \/ Unit)

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
}

private[activation] class InternalCodeActivation(activationActor: ActorRef, config: ActivationConfig)(implicit db: Database, ec: ExecutionContext) extends CodeActivation {

  import InternalCodeActivation._
  import im.actor.server.activation.Activation._

  implicit val timeout: Timeout = Timeout(20.seconds)

  def send(transactionHash: Option[String], code: Code): DBIO[String \/ Unit] = (transactionHash match {
    case Some(hash) ⇒ for (_ ← persist.AuthCodeRepo.createOrUpdate(hash, code.code)) yield ()
    case None       ⇒ DBIO.successful(())
  }) flatMap (_ ⇒ DBIO.from(sendCode(code)))

  def validate(transactionHash: String, code: String): DBIO[ValidationResponse] =
    for {
      optCode ← persist.AuthCodeRepo.findByTransactionHash(transactionHash)
      result ← optCode map {
        case s if isExpired(s) ⇒
          for (_ ← persist.AuthCodeRepo.deleteByTransactionHash(transactionHash)) yield ExpiredCode
        case s if s.code != code ⇒
          if (s.attempts + 1 >= config.attempts) {
            for (_ ← persist.AuthCodeRepo.deleteByTransactionHash(transactionHash)) yield ExpiredCode
          } else {
            for (_ ← persist.AuthCodeRepo.incrementAttempts(transactionHash, s.attempts)) yield InvalidCode
          }
        case _ ⇒ DBIO.successful(Validated)
      } getOrElse DBIO.successful(InvalidHash)
    } yield result

  def finish(transactionHash: String): DBIO[Unit] = persist.AuthCodeRepo.deleteByTransactionHash(transactionHash).map(_ ⇒ ())

  private def isExpired(code: AuthCode): Boolean =
    code.createdAt.plus(config.expiration.toMillis, MILLIS).isBefore(LocalDateTime.now(ZoneOffset.UTC))

  private def sendCode(code: Code): Future[String \/ Unit] =
    code match {
      case p: PhoneCode if isTestPhone(p.phone) ⇒ Future.successful(\/-(()))
      case _                                    ⇒ (activationActor ? Send(code)).mapTo[SendAck].map(_.result)
    }

  private def isTestPhone(number: Long): Boolean = number.toString.startsWith("7555")
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
      val replyTo = sender()
      sendCode(code) foreach { resp ⇒ replyTo ! SendAck(resp) }
    case ForgetSentCode(code) ⇒ forgetSentCode(code)
  }

  private def sendCode(code: Code): Future[String \/ Unit] = {
    if (codeWasNotSent(code)) {
      log.debug(s"Sending $code")

      rememberSentCode(code)

      (code match {
        case SmsCode(phone, c)            ⇒ smsEngine.sendCode(phone, c)
        case CallCode(phone, c, language) ⇒ callEngine.sendCode(phone, c, language)
        case EmailCode(email, c) ⇒
          emailSender.send(Message(email, "Actor activation code", emailTemplate.replace("$$CODE$$", c)))
      }) map { _ ⇒
        forgetSentCodeAfterDelay(code)
        \/-(())
      } recover { case e ⇒ -\/("Unable to send code") }
    } else {
      log.debug(s"Ignoring send $code")
      Future.successful(-\/("Attempt to get code later"))
    }
  }

}
