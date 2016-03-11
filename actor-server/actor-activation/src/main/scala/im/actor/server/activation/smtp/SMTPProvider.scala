package im.actor.server.activation.smtp

import java.nio.file.{ Files, Paths }

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import cats.data.Xor
import im.actor.config.ActorConfig
import im.actor.env.ActorEnv
import im.actor.server.activation.common.ActivationStateActor.{ Send, SendAck }
import im.actor.server.activation.common._
import im.actor.server.db.DbExtension
import im.actor.server.email.{ Content, EmailConfig, Message, SmtpEmailSender }
import im.actor.util.misc.EmailUtils.isTestEmail

import scala.concurrent.Future
import scala.concurrent.duration._

private[activation] final class SMTPProvider(system: ActorSystem) extends ActivationProvider with CommonAuthCodes {

  protected implicit val ec = system.dispatcher
  protected val activationConfig = ActivationConfig.load.getOrElse(throw new RuntimeException("Failed to load activation config"))
  protected val db = DbExtension(system).db

  private implicit val timeout = Timeout(20.seconds)

  private val emailConfig = EmailConfig.load.getOrElse(throw new RuntimeException("Failed to load email config"))
  private val emailSender = new SmtpEmailSender(emailConfig)
  private val emailTemplateLocation =
    ActorEnv.getAbsolutePath(Paths.get(ActorConfig.load().getString("services.activation.email.template")))

  private val emailTemplate = new String(Files.readAllBytes(emailTemplateLocation))

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
        Future.successful(Xor.right(()))
      } else {
        for {
          resp ← (smtpStateActor ? Send(code)).mapTo[SendAck].map(_.result)
          _ ← createAuthCodeIfNeeded(resp, txHash, code.code)
        } yield resp
      }
    case other ⇒ throw new RuntimeException(s"This provider can't handle code of type: ${other.getClass}")
  }

}
