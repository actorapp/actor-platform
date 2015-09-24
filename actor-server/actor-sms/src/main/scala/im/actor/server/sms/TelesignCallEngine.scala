package im.actor.server.sms

import scala.concurrent.Future

class TelesignCallEngine(telesignClient: TelesignClient) extends AuthCallEngine {
  override def sendCode(phoneNumber: Long, code: String, language: String): Future[Unit] = telesignClient.sendCallCode(phoneNumber, code, language)
}
