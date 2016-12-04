package im.actor.server.sms

import scala.concurrent.Future

final class MagfaSmsEngine(magfaSmsClient: MagfaClient) extends AuthSmsEngine {
  override def sendCode(phoneNumber: Long, message: String): Future[Unit] = magfaSmsClient.sendSmsCode(phoneNumber, message)
}