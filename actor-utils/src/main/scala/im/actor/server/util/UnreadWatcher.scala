package im.actor.server.util

import java.util.concurrent.TimeUnit

import scala.concurrent.duration._

import com.typesafe.config.Config
import org.joda.time.DateTime
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

import im.actor.server.models.PeerType
import im.actor.server.sms.SmsEngine
import im.actor.server.{ models, persist }
import scala.concurrent.ExecutionContext.Implicits.global

case class UnreadWatcherConfig(unreadTimeout: Duration)

object UnreadWatcherConfig {
  def apply(config: Config): UnreadWatcherConfig =
    UnreadWatcherConfig.apply(config.getDuration("unread-timeout", TimeUnit.HOURS).minutes)
}

trait Notifier {
  def processTask(task: Notication): Unit
}

class PhoneNotifier(engine: SmsEngine) extends Notifier {
  def processTask(task: Notication) = {
    val total = task.data.values.sum
    val senders = task.data.keySet.filter(_.isDefined).map(_.get) mkString " ,"
    val message = s"You got $total messages from $senders."
    for {
      optPhone ← persist.UserPhone.findByUserId(task.userId).headOption
      _ ← DBIO.successful(optPhone.map { phone ⇒ engine.send(phone.number, message) })
    } yield ()
  }
}

case class Notication(userId: Int, data: Map[Option[String], Int])

class UnreadWatcher(implicit db: Database, config: UnreadWatcherConfig, notifier: Notifier) {

  val unreadTimeout = config.unreadTimeout.toMillis

  def notifyUsers() = {
    db.run {
      for {
        users ← persist.User.activeUsersIds
        tasks ← DBIO.sequence(users.map { userId ⇒
          for (data ← findUnread(userId)) yield Notication(userId, data.toMap)
        })
      } yield tasks
    }.map { tasks ⇒
      tasks.foreach(notifier.processTask)
    }
    ()
  }

  private def findUnread(userId: Int) = {
    val dateToReadBefore = DateTime.now.minus(unreadTimeout)
    for {
      dialogs ← persist.Dialog.findLastReadBefore(dateToReadBefore, userId)
      senderAndCount ← DBIO.sequence(dialogs.map { dialog ⇒
        for {
          exists ← persist.HistoryMessage.haveMessagesBetween(userId, dialog.peer, dialog.lastReadAt, dateToReadBefore)
          unreadCount ← persist.HistoryMessage.getUnreadCount(userId, dialog.peer, dialog.lastReadAt)
          senderName ← getNameByPeer(dialog.peer)
          if exists
        } yield senderName → unreadCount
      })
    } yield senderAndCount
  }

  private def getNameByPeer(peer: models.Peer) =
    if (peer.typ == PeerType.Private) persist.User.findName(peer.id)
    else persist.Group.findTitle(peer.id)

}
