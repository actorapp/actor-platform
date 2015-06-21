package im.actor.server.sms

import scala.concurrent.Future

trait SmsEngine {
  def send(phoneNumber: Long, message: String): Future[Unit]
}
