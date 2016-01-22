package im.actor.server.notifications

import akka.actor.ActorSystem
import im.actor.server.db.DbExtension
import im.actor.server.group.GroupExtension
import im.actor.server.model.PeerType
import im.actor.server.persist.dialog.DialogRepo
import im.actor.server.user.UserExtension
import im.actor.server.{ model, persist }
import org.joda.time.DateTime
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

import scala.concurrent.Future

private class UnreadWatcher()(implicit system: ActorSystem, config: UnreadWatcherConfig) {
  import system.dispatcher

  private val db: Database = DbExtension(system).db
  private val unreadTimeout = config.unreadTimeout.toMillis

  def getNotifications: Future[Seq[Notification]] = {
    val now = DateTime.now
    db.run {
      for {
        users ← persist.UserRepo.activeUsersIds
        tasks ← DBIO.sequence(users.map { userId ⇒
          for (data ← findUnread(userId, now)) yield if (data.isEmpty) None else Some(Notification(userId, data.toMap))
        })
      } yield tasks.flatten
    }
  }

  private def findUnread(userId: Int, now: DateTime): DBIO[Seq[(Option[String], Int)]] = {
    val dateToReadBefore = now.minus(unreadTimeout)
    for {
      dialogs ← DialogRepo.findLastReadBefore(dateToReadBefore, userId)
      senderAndCount ← DBIO.sequence(dialogs.map { dialog ⇒
        for {
          exists ← persist.HistoryMessageRepo.haveMessagesBetween(userId, dialog.peer, dialog.ownerLastReadAt, dateToReadBefore)
          unreadCount ← persist.HistoryMessageRepo.getUnreadCount(userId, dialog.peer, dialog.ownerLastReadAt, noServiceMessages = true)
          senderName ← DBIO.from(getNameByPeer(userId, dialog.peer))
        } yield if (exists) Some(senderName → unreadCount) else None
      })
    } yield senderAndCount.flatten
  }

  private def getNameByPeer(userId: Int, peer: model.Peer): Future[Option[String]] = {
    (if (peer.typ == PeerType.Private)
      UserExtension(system).getApiStruct(peer.id, userId, 0L) map (u ⇒ u.localName.getOrElse(u.name))
    else GroupExtension(system).getApiStruct(peer.id, userId) map (_.title)) map (Some(_))
  }

}