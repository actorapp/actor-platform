package im.actor.server.sms

import scala.concurrent.Future

trait AuthSmsEngine {
  def sendCode(phoneNumber: Long, code: String): Future[Unit]
}