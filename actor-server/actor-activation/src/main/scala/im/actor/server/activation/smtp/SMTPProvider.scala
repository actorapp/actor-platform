package im.actor.server.activation.smtp

import java.nio.file.{ Files, Paths }

import akka.actor.ActorSystem
import akka.http.scaladsl.util.FastFuture
import akka.pattern.ask
import akka.util.Timeout
import cats.data.Xor
import im.actor.config.ActorConfig
import im.actor.env.ActorEnv
import im.actor.server.activation.common.ActivationStateActor.{ ForgetSentCode, Send, SendAck }
import im.actor.server.activation.common._
import im.actor.server.db.DbExtension
import im.actor.server.email._
import im.actor.server.model.AuthEmailTransaction
import im.actor.server.persist.auth.AuthTransactionRepo
import im.actor.util.misc.EmailUtils.isTestEmail

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Try

private[activation] final class SMTPProvider(system: ActorSystem) extends ActivationProvider with CommonAuthCodes {

  protected implicit val ec = system.dispatcher
  protected val activationConfig = ActivationConfig.load.getOrElse(throw new RuntimeException("Failed to load activation config"))
  protected val db = DbExtension(system).db

  private implicit val timeout = Timeout(20.seconds)

  private val emailSender = EmailExtension(system).sender

  private val emailTemplateLocation =
    ActorEnv.getAbsolutePath(Paths.get(ActorConfig.load().getString("services.activation.email.template")))

  private val emailTemplate =
    Try(new String(Files.readAllBytes(emailTemplateLocation)))
      .getOrElse(throw new RuntimeException(s"Failed to read template file. Make sure you put it in ${emailTemplateLocation}"))

  private val smtpStateActor = system.actorOf(ActivationStateActor.props[String, EmailCode](
    repeatLimit = activationConfig.repeatLimit,
    sendAction = (code: EmailCode) ⇒ emailSender.send(
    Message(
      to = code.email,
      subject = s"Actor activation code: ${code.code}",
      content = Content(Some(emailTemplate.replace("$$CODE$$", code.code)), Some(s"Your actor activation code: ${code.code}"))
    )
  ),
    id = (code: EmailCode) ⇒ code.email
  ), "internal-smtp-state")

  override def send(txHash: String, code: Code): Future[CodeFailure Xor Unit] = code match {
    case code: EmailCode ⇒
      if (isTestEmail(code.email)) {
        FastFuture.successful(Xor.right(()))
      } else {
        for {
          resp ← (smtpStateActor ? Send(code)).mapTo[SendAck].map(_.result)
          _ ← createAuthCodeIfNeeded(resp, txHash, code.code)
        } yield resp
      }
    case other ⇒ throw new RuntimeException(s"This provider can't handle code of type: ${other.getClass}")
  }

  override def cleanup(txHash: String): Future[Unit] = {
    for {
      ac ← db.run(AuthTransactionRepo.findChildren(txHash))
      _ = ac match {
        case Some(x: AuthEmailTransaction) ⇒ smtpStateActor ! ForgetSentCode.email(x.email)
        case _                             ⇒
      }
      _ ← deleteAuthCode(txHash)
    } yield ()
  }

}
