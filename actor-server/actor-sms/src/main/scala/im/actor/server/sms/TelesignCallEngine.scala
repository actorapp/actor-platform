package im.actor.server.sms

import scala.concurrent.Future

class TelesignCallEngine(telesignClient: TelesignClient) extends AuthCallEngine {
  override def sendCode(phoneNumber: Long, message: String, language: String): Future[Unit] = telesignClient.sendSmsCode(phoneNumber, message, language)
}
