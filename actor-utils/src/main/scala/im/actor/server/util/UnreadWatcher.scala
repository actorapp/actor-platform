package im.actor.server.util

import java.util.concurrent.TimeUnit

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import com.typesafe.config.Config
import org.joda.time.DateTime
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

import im.actor.server.models.PeerType
import im.actor.server.sms.SmsEngine
import im.actor.server.{ models, persist }

case class UnreadWatcherConfig(unreadTimeout: Duration)

object UnreadWatcherConfig {
  def apply(config: Config): UnreadWatcherConfig =
    UnreadWatcherConfig.apply(config.getDuration("unread-timeout", TimeUnit.HOURS).minutes)
}

trait Notifier {
  def processTask(task: Notification): Unit
}

class PhoneNotifier(engine: SmsEngine) extends Notifier {
  def processTask(task: Notification) = {
    val total = task.data.values.sum
    val senders = task.data.keySet.flatten mkString " ,"
    val message = s"You got $total messages from $senders."
    for {
      optPhone ← persist.UserPhone.findByUserId(task.userId).headOption
      _ ← DBIO.successful(optPhone.map { phone ⇒ engine.send(phone.number, message) })
    } yield ()
  }
}

case class Notification(userId: Int, data: Map[Option[String], Int])

class UnreadWatcher(implicit db: Database, config: UnreadWatcherConfig, notifier: Notifier) {

  val unreadTimeout = config.unreadTimeout.toMillis

  def notifyUsers() = {
    getNotifications.map { tasks ⇒
      tasks.foreach(notifier.processTask)
    }
  }

  def getNotifications = {
    val now = DateTime.now
    db.run {
      for {
        users ← persist.User.activeUsersIds
        tasks ← DBIO.sequence(users.map { userId ⇒
          for (data ← findUnread(userId, now)) yield if (data.isEmpty) None else Some(Notification(userId, data.toMap))
        })
      } yield tasks.flatten
    }
  }

  private def findUnread(userId: Int, now: DateTime) = {
    val dateToReadBefore = now.minus(unreadTimeout)
    for {
      dialogs ← persist.Dialog.findLastReadBefore(dateToReadBefore, userId)
      senderAndCount ← DBIO.sequence(dialogs.map { dialog ⇒
        for {
          exists ← persist.HistoryMessage.haveMessagesBetween(userId, dialog.peer, dialog.lastReadAt, dateToReadBefore)
          unreadCount ← persist.HistoryMessage.getUnreadCount(userId, dialog.peer, dialog.lastReadAt)
          senderName ← getNameByPeer(dialog.peer)
        } yield if (exists) Some(senderName → unreadCount) else None
      })
    } yield senderAndCount.flatten
  }

  private def getNameByPeer(peer: models.Peer) =
    if (peer.typ == PeerType.Private) persist.User.findName(peer.id)
    else persist.Group.findTitle(peer.id)

}
