package im.actor.server.persist

import scala.concurrent.ExecutionContext

import com.github.tototoshi.slick.PostgresJodaSupport._
import org.joda.time.DateTime
import slick.dbio.Effect.{ Read, Write }
import slick.driver.PostgresDriver.api._
import slick.profile.{ SqlAction, FixedSqlStreamingAction, FixedSqlAction }

import im.actor.server.models

class DialogTable(tag: Tag) extends Table[models.Dialog](tag, "dialogs") {

  def userId = column[Int]("user_id", O.PrimaryKey)

  def peerType = column[Int]("peer_type", O.PrimaryKey)

  def peerId = column[Int]("peer_id", O.PrimaryKey)

  def lastMessageDate = column[DateTime]("last_message_date")

  def lastReceivedAt = column[DateTime]("last_received_at")

  def lastReadAt = column[DateTime]("last_read_at")

  def ownerLastReceivedAt = column[DateTime]("owner_last_received_at")

  def ownerLastReadAt = column[DateTime]("owner_last_read_at")

  def * = (userId, peerType, peerId, lastMessageDate, lastReceivedAt, lastReadAt, ownerLastReceivedAt, ownerLastReadAt) <> (applyDialog.tupled, unapplyDialog)

  def applyDialog: (Int, Int, Int, DateTime, DateTime, DateTime, DateTime, DateTime) ⇒ models.Dialog = {
    case (userId, peerType, peerId, lastMessageDate, lastReceivedAt, lastReadAt, ownerLastReceivedAt, ownerLastReadAt) ⇒
      models.Dialog(
        userId = userId,
        peer = models.Peer(models.PeerType.fromInt(peerType), peerId),
        lastMessageDate = lastMessageDate,
        lastReceivedAt = lastReceivedAt,
        lastReadAt = lastReadAt,
        ownerLastReceivedAt = ownerLastReceivedAt,
        ownerLastReadAt = ownerLastReadAt
      )
  }

  def unapplyDialog: models.Dialog ⇒ Option[(Int, Int, Int, DateTime, DateTime, DateTime, DateTime, DateTime)] = { dialog ⇒
    models.Dialog.unapply(dialog).map {
      case (userId, peer, lastMessageDate, lastReceivedAt, lastReadAt, ownerLastReceivedAt, ownerLastReadAt) ⇒
        (userId, peer.typ.toInt, peer.id, lastMessageDate, lastReceivedAt, lastReadAt, ownerLastReceivedAt, ownerLastReadAt)
    }
  }
}

object Dialog {
  val dialogs = TableQuery[DialogTable]

  def byUserIdPeer(userId: Int, peer: models.Peer) =
    dialogs.filter(d ⇒ d.userId === userId && d.peerType === peer.typ.toInt && d.peerId === peer.id)

  def create(dialog: models.Dialog) =
    dialogs += dialog

  def createIfNotExists(dialog: models.Dialog)(implicit ec: ExecutionContext) = {
    for {
      dOpt ← find(dialog.userId, dialog.peer)
      res ← if (dOpt.isEmpty) create(dialog) else DBIO.successful(0)
    } yield res
  }

  def find(userId: Int, peer: models.Peer): SqlAction[Option[models.Dialog], NoStream, Read] =
    dialogs.filter(d ⇒ d.userId === userId && d.peerType === peer.typ.toInt && d.peerId === peer.id).result.headOption

  def findLastReadBefore(date: DateTime, userId: Int) =
    dialogs.filter(d ⇒ d.userId === userId && d.ownerLastReadAt < date).result

  def findByUser(userId: Int, dateOpt: Option[DateTime], limit: Int) = {
    val baseQuery = dialogs
      .filter(d ⇒ d.userId === userId)

    val query = dateOpt match {
      case Some(date) ⇒
        baseQuery.filter(_.lastMessageDate <= date).sortBy(_.lastMessageDate.desc)
      case None ⇒
        baseQuery.sortBy(_.lastMessageDate.desc)
    }

    query.take(limit).result
  }

  def updateLastMessageDate(userId: Int, peer: models.Peer, lastMessageDate: DateTime)(implicit ec: ExecutionContext) = {
    byUserIdPeer(userId, peer).map(_.lastMessageDate).update(lastMessageDate) flatMap {
      case 0 ⇒
        create(models.Dialog(userId, peer, lastMessageDate, new DateTime(0), new DateTime(0), new DateTime(0), new DateTime(0)))
      case x ⇒ DBIO.successful(x)
    }
  }

  def findExistingUserIds(userIds: Set[Int], peer: models.Peer): FixedSqlStreamingAction[Seq[Int], Int, Read] = {
    dialogs.filter(d ⇒ (d.userId inSet userIds) && d.peerType === peer.typ.toInt && d.peerId === peer.id).map(_.userId).result
  }

  def updateLastMessageDates(userIds: Set[Int], peer: models.Peer, lastMessageDate: DateTime)(implicit ec: ExecutionContext) = {
    for {
      existing ← findExistingUserIds(userIds, peer) map (_.toSet)
      _ ← dialogs
        .filter(d ⇒ d.peerType === peer.typ.toInt && d.peerId === peer.id && (d.userId inSet existing))
        .map(_.lastMessageDate)
        .update(lastMessageDate)
      _ ← DBIO.sequence(
        (userIds diff existing)
          .toSeq
          .map(userId ⇒ create(models.Dialog(userId, peer, lastMessageDate, new DateTime(0), new DateTime(0), new DateTime(0), new DateTime(0))))
      )
    } yield userIds.size
  }

  def updateLastReceivedAt(userId: Int, peer: models.Peer, lastReceivedAt: DateTime)(implicit ec: ExecutionContext) = {
    byUserIdPeer(userId, peer).map(_.lastReceivedAt).update(lastReceivedAt) flatMap {
      case 0 ⇒
        create(models.Dialog(userId, peer, new DateTime(0), lastReceivedAt, new DateTime(0), new DateTime(0), new DateTime(0)))
      case x ⇒ DBIO.successful(x)
    }
  }

  def updateOwnerLastReceivedAt(userId: Int, peer: models.Peer, ownerLastReceivedAt: DateTime)(implicit ec: ExecutionContext) = {
    byUserIdPeer(userId, peer).map(_.ownerLastReceivedAt).update(ownerLastReceivedAt) flatMap {
      case 0 ⇒
        create(models.Dialog(userId, peer, new DateTime(0), new DateTime(0), new DateTime(0), ownerLastReceivedAt, new DateTime(0)))
      case x ⇒ DBIO.successful(x)
    }
  }

  def updateLastReadAt(userId: Int, peer: models.Peer, lastReadAt: DateTime)(implicit ec: ExecutionContext) = {
    byUserIdPeer(userId, peer).map(_.lastReadAt).update(lastReadAt) flatMap {
      case 0 ⇒
        create(models.Dialog(userId, peer, new DateTime(0), new DateTime(0), lastReadAt, new DateTime(0), new DateTime(0)))
      case x ⇒ DBIO.successful(x)
    }
  }

  def updateOwnerLastReadAt(userId: Int, peer: models.Peer, ownerLastReadAt: DateTime)(implicit ec: ExecutionContext) = {
    byUserIdPeer(userId, peer).map(_.ownerLastReadAt).update(ownerLastReadAt) flatMap {
      case 0 ⇒
        create(models.Dialog(userId, peer, new DateTime(0), new DateTime(0), new DateTime(0), new DateTime(0), ownerLastReadAt))
      case x ⇒ DBIO.successful(x)
    }
  }

  def delete(userId: Int, peer: models.Peer): FixedSqlAction[Int, NoStream, Write] =
    byUserIdPeer(userId, peer).delete
}
