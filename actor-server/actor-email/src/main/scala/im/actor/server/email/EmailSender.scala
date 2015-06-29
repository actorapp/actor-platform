package im.actor.server.email

import scala.concurrent.{ ExecutionContext, Future }

import org.apache.commons.mail.{ DefaultAuthenticator, SimpleEmail }

case class Message(to: String, subject: String, content: String)

class EmailSender(config: EmailConfig) {
  def send(message: Message)(implicit ec: ExecutionContext) = Future {
    val email = new SimpleEmail()
    email.setHostName(config.hostname)
    email.setSmtpPort(config.smtpPort)
    email.setAuthenticator(new DefaultAuthenticator(config.username, config.password))
    email.setSSLOnConnect(true)

    email.setFrom(config.from)
    email.setSubject(message.subject)
    email.setMsg(message.content)
    email.addTo(message.to)
    email.send()
  }
}