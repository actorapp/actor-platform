package im.actor.server.notifications

import scala.concurrent.ExecutionContext

import slick.dbio.DBIO

import im.actor.server.persist
import im.actor.server.sms.SmsEngine
import slick.driver.PostgresDriver.api._

trait Notifier {
  def processTask(task: Notification): Unit
}

class PhoneNotifier(engine: SmsEngine)(implicit db: Database, ec: ExecutionContext) extends Notifier {
  def processTask(task: Notification): Unit = {
    val total = task.data.values.sum
    val senders = task.data.keySet.flatten mkString ", "
    val message = s"You got $total messages from $senders."
    db.run {
      for {
        optPhone ← persist.UserPhone.findByUserId(task.userId).headOption
        _ ← DBIO.successful(optPhone.map { phone ⇒ engine.send(phone.number, message) })
      } yield ()
    }
  }
}
