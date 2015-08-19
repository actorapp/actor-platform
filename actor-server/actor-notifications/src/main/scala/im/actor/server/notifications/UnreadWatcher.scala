package im.actor.server.notifications

import akka.util.Timeout
import im.actor.server.group.{ GroupOffice, GroupViewRegion }
import im.actor.server.models.PeerType
import im.actor.server.user.{ UserOffice, UserViewRegion }
import im.actor.server.{ models, persist }
import org.joda.time.DateTime
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

import scala.concurrent.{ ExecutionContext, Future }

class UnreadWatcher(implicit
  db: Database,
                    config:      UnreadWatcherConfig,
                    ec:          ExecutionContext,
                    userRegion:  UserViewRegion,
                    timeout:     Timeout,
                    groupRegion: GroupViewRegion) {

  private val unreadTimeout = config.unreadTimeout.toMillis

  def getNotifications: Future[Seq[Notification]] = {
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

  private def findUnread(userId: Int, now: DateTime): DBIO[Seq[(Option[String], Int)]] = {
    val dateToReadBefore = now.minus(unreadTimeout)
    for {
      dialogs ← persist.Dialog.findLastReadBefore(dateToReadBefore, userId)
      senderAndCount ← DBIO.sequence(dialogs.map { dialog ⇒
        for {
          exists ← persist.HistoryMessage.haveMessagesBetween(userId, dialog.peer, dialog.ownerLastReadAt, dateToReadBefore)
          unreadCount ← persist.HistoryMessage.getUnreadCount(userId, dialog.peer, dialog.ownerLastReadAt, noServiceMessages = true)
          senderName ← DBIO.from(getNameByPeer(userId, dialog.peer))
        } yield if (exists) Some(senderName → unreadCount) else None
      })
    } yield senderAndCount.flatten
  }

  private def getNameByPeer(userId: Int, peer: models.Peer): Future[Option[String]] = {
    (if (peer.typ == PeerType.Private)
      UserOffice.getApiStruct(peer.id, userId, 0L) map (u ⇒ u.localName.getOrElse(u.name))
    else GroupOffice.getApiStruct(peer.id, userId) map (_.title)) map (Some(_))
  }

}