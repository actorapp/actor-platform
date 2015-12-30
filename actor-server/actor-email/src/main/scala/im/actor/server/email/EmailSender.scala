package im.actor.server.email

import im.actor.config.ActorConfig
import org.apache.commons.mail.{ DefaultAuthenticator, HtmlEmail }

import scala.concurrent.{ ExecutionContext, Future }

/**
 * describes content of email. One of content entries should be present. If both are present - both will be set
 *
 * @param html email html content. Should be present if you want to send message with html
 * @param text plain text content
 */
case class Content(html: Option[String], text: Option[String]) {
  require(html.isDefined || text.isDefined)
}

object Content {
  def text(text: String): Content = Content(None, Some(text))
  def html(html: String): Content = Content(Some(html), None)
}

case class Message(to: String, subject: String, content: Content)

trait EmailSender {
  def send(message: Message): Future[Unit]
}

final class DummyEmailSender extends EmailSender {
  override def send(message: Message): Future[Unit] = Future.successful(())
}

final class SmtpEmailSender(config: EmailConfig)(implicit ec: ExecutionContext) extends EmailSender {
  val timeout = (ActorConfig.defaultTimeout.toMillis / 2).toInt
  override def send(message: Message) = Future {
    val email = new HtmlEmail()
    email.setSocketTimeout(timeout)
    email.setSocketConnectionTimeout(timeout)
    email.setCharset("UTF-8")
    email.setHostName(config.host)
    email.setSmtpPort(config.port)
    email.setAuthenticator(new DefaultAuthenticator(config.username, config.password))
    email.setStartTLSEnabled(config.tls)
    email.setFrom(config.address, config.name)
    email.setSubject(message.subject)
    message.content.html foreach { email.setHtmlMsg }
    message.content.text foreach { email.setTextMsg }
    email.addTo(message.to)
    email.send()
  }
}