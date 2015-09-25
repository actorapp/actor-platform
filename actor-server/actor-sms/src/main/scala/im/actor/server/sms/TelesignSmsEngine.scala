package im.actor.server.sms

import scala.concurrent.Future

final class TelesignSmsEngine(telesignClient: TelesignClient) extends AuthSmsEngine {
  override def sendCode(phoneNumber: Long, message: String): Future[Unit] = telesignClient.sendSmsCode(phoneNumber, message, TelesignClient.DefaultSmsTemplate)
}
