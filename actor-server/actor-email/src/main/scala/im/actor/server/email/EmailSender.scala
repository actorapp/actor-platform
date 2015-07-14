package im.actor.server.email

import scala.concurrent.{ Future, ExecutionContext }

trait EmailSender {
  def send(message: Message)(implicit ec: ExecutionContext): Future[Unit]
}
