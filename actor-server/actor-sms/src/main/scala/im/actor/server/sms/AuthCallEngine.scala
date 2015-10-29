package im.actor.server.sms

import scala.concurrent.Future

trait AuthCallEngine {
  def sendCode(phoneNumber: Long, code: String, language: String): Future[Unit]
}
