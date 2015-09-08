package im.actor.server.notifications

import scala.concurrent.ExecutionContext

import slick.dbio.DBIO

import im.actor.server.persist
import im.actor.server.sms.SmsEngine
import slick.driver.PostgresDriver.api._

import im.actor.util.misc.StringUtils._

trait Notifier {
  def processTask(task: Notification): Unit
}

class PhoneNotifier(engine: SmsEngine)(implicit db: Database, ec: ExecutionContext) extends Notifier {
  def processTask(task: Notification): Unit =
    db.run {
      for {
        optPhone ← persist.UserPhone.findByUserId(task.userId).headOption
        prodPhone = optPhone.filter(!_.number.toString.startsWith("7555")).map(_.number)
        _ ← DBIO.successful(prodPhone.map { engine.send(_, makeMessage(task.data)) })
      } yield ()
    }

  private def makeMessage(data: Map[Option[String], Int]): String = {
    val total = data.values.sum
    val senders = data.keySet.flatten

    val message = s"You got $total messages from ${senders mkString ", "} in Actor."
    isAsciiString(message) match {
      case true if message.length <= 140 ⇒ message
      case false if message.length <= 70 ⇒ message
      case _                             ⇒ s"You got $total messages in ${senders.size} dialogs in Actor."
    }
  }

}
