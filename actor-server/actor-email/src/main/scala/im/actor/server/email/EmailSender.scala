package im.actor.server.email

import scala.concurrent.{ ExecutionContext, Future }

import org.apache.commons.mail.{ EmailConstants, DefaultAuthenticator, SimpleEmail }

case class Message(to: String, subject: String, content: String)

class EmailSender(config: EmailConfig) {
  def send(message: Message)(implicit ec: ExecutionContext) = Future {
    val email = new SimpleEmail()
    email.setHostName(config.host)
    email.setSmtpPort(config.port)
    email.setAuthenticator(new DefaultAuthenticator(config.username, config.password))
    email.setStartTLSEnabled(config.tls)

    email.setFrom(config.address)
    email.setSubject(message.subject)
    email.setContent(message.content, EmailConstants.TEXT_PLAIN)
    email.addTo(message.to)
    email.send()
  }
}