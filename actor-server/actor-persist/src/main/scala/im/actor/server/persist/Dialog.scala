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

  def byPeerSimple(peerType: Rep[Int], peerId: Rep[Int]) =
    dialogs.filter(d ⇒ d.peerType === peerType && d.peerId === peerId)

  def byPKSimple(userId: Rep[Int], peerType: Rep[Int], peerId: Rep[Int]) =
    dialogs.filter(d ⇒ d.userId === userId && d.peerType === peerType && d.peerId === peerId)

  def byPK(userId: Int, peer: models.Peer) =
    byPKSimple(userId, peer.typ.toInt, peer.id)

  def byPeerType(userId: Rep[Int], peerType: Rep[Int]) =
    dialogs.filter(d ⇒ d.userId === userId && d.peerType === peerType)

  def idByPeerType(userId: Rep[Int], peerType: Rep[Int]) =
    byPeerType(userId, peerType).map(_.peerId)

  val byPKC = Compiled(byPKSimple _)
  val byPeerC = Compiled(byPeerSimple _)
  val byPeerTypeC = Compiled(byPeerType _)
  val idByPeerTypeC = Compiled(idByPeerType _)

  def create(dialog: models.Dialog) =
    dialogs += dialog

  def createIfNotExists(dialog: models.Dialog)(implicit ec: ExecutionContext) = {
    for {
      dOpt ← find(dialog.userId, dialog.peer)
      res ← if (dOpt.isEmpty) create(dialog) else DBIO.successful(0)
    } yield res
  }

  def find(userId: Int, peer: models.Peer): SqlAction[Option[models.Dialog], NoStream, Read] =
    byPKC((userId, peer.typ.toInt, peer.id)).result.headOption

  def findGroups(userId: Int): FixedSqlStreamingAction[Seq[models.Dialog], models.Dialog, Read] =
    byPeerTypeC((userId, models.PeerType.Group.toInt)).result

  def findGroupIds(userId: Int): FixedSqlStreamingAction[Seq[Int], Int, Read] =
    idByPeerTypeC((userId, models.PeerType.Group.toInt)).result

  def findUserIds(userId: Int): FixedSqlStreamingAction[Seq[Int], Int, Read] =
    idByPeerTypeC((userId, models.PeerType.Private.toInt)).result

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
    byPKC.applied((userId, peer.typ.toInt, peer.id)).map(_.lastMessageDate).update(lastMessageDate) flatMap {
      case 0 ⇒
        create(models.Dialog(userId, peer, lastMessageDate, new DateTime(0), new DateTime(0), new DateTime(0), new DateTime(0)))
      case x ⇒ DBIO.successful(x)
    }
  }

  def findExistingUserIds(userIds: Set[Int], peer: models.Peer): FixedSqlStreamingAction[Seq[Int], Int, Read] = {
    byPeerC.applied((peer.typ.toInt, peer.id))
      .filter(_.userId inSetBind userIds)
      .map(_.userId)
      .result
  }

  def updateLastMessageDates(userIds: Set[Int], peer: models.Peer, lastMessageDate: DateTime)(implicit ec: ExecutionContext) = {
    for {
      existing ← findExistingUserIds(userIds, peer) map (_.toSet)
      _ ← byPeerC.applied((peer.typ.toInt, peer.id))
        .filter(_.userId inSetBind existing)
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
    byPKC.applied((userId, peer.typ.toInt, peer.id)).map(_.lastReceivedAt).update(lastReceivedAt) flatMap {
      case 0 ⇒
        create(models.Dialog(userId, peer, new DateTime(0), lastReceivedAt, new DateTime(0), new DateTime(0), new DateTime(0)))
      case x ⇒ DBIO.successful(x)
    }
  }

  def updateLastReceivedAt(userIds: Set[Int], peer: models.Peer, lastReceivedAt: DateTime)(implicit ec: ExecutionContext) = {
    for {
      existing ← findExistingUserIds(userIds, peer) map (_.toSet)
      _ ← byPeerC.applied((peer.typ.toInt, peer.id))
        .filter(_.userId inSetBind existing)
        .map(_.lastReceivedAt)
        .update(lastReceivedAt)
      _ ← DBIO.sequence(
        (userIds diff existing)
          .toSeq
          .map(userId ⇒ create(models.Dialog(userId, peer, new DateTime(0), lastReceivedAt, new DateTime(0), new DateTime(0), new DateTime(0))))
      )
    } yield userIds.size
  }

  def updateOwnerLastReceivedAt(userId: Int, peer: models.Peer, ownerLastReceivedAt: DateTime)(implicit ec: ExecutionContext) = {
    byPKC.applied((userId, peer.typ.toInt, peer.id)).map(_.ownerLastReceivedAt).update(ownerLastReceivedAt) flatMap {
      case 0 ⇒
        create(models.Dialog(userId, peer, new DateTime(0), new DateTime(0), new DateTime(0), ownerLastReceivedAt, new DateTime(0)))
      case x ⇒ DBIO.successful(x)
    }
  }

  def updateLastReadAt(userId: Int, peer: models.Peer, lastReadAt: DateTime)(implicit ec: ExecutionContext) = {
    byPKC.applied((userId, peer.typ.toInt, peer.id)).map(_.lastReadAt).update(lastReadAt) flatMap {
      case 0 ⇒
        create(models.Dialog(userId, peer, new DateTime(0), new DateTime(0), lastReadAt, new DateTime(0), new DateTime(0)))
      case x ⇒ DBIO.successful(x)
    }
  }

  def updateLastReadAt(userIds: Set[Int], peer: models.Peer, lastReadAt: DateTime)(implicit ec: ExecutionContext) = {
    for {
      existing ← findExistingUserIds(userIds, peer) map (_.toSet)
      _ ← byPeerC.applied((peer.typ.toInt, peer.id))
        .filter(_.userId inSetBind existing)
        .map(_.lastReadAt)
        .update(lastReadAt)
      _ ← DBIO.sequence(
        (userIds diff existing)
          .toSeq
          .map(userId ⇒ create(models.Dialog(userId, peer, new DateTime(0), new DateTime(0), lastReadAt, new DateTime(0), new DateTime(0))))
      )
    } yield userIds.size
  }

  def updateOwnerLastReadAt(userId: Int, peer: models.Peer, ownerLastReadAt: DateTime)(implicit ec: ExecutionContext) = {
    byPKC.applied((userId, peer.typ.toInt, peer.id)).map(_.ownerLastReadAt).update(ownerLastReadAt) flatMap {
      case 0 ⇒
        create(models.Dialog(userId, peer, new DateTime(0), new DateTime(0), new DateTime(0), new DateTime(0), ownerLastReadAt))
      case x ⇒ DBIO.successful(x)
    }
  }

  def delete(userId: Int, peer: models.Peer): FixedSqlAction[Int, NoStream, Write] =
    byPKC.applied((userId, peer.typ.toInt, peer.id)).delete
}
