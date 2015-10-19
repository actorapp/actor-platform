package im.actor.server.persist

import scala.concurrent.ExecutionContext

import com.github.tototoshi.slick.PostgresJodaSupport._
import org.joda.time.DateTime
import slick.dbio.Effect.{ Read, Write }
import slick.driver.PostgresDriver.api._
import slick.profile.{ SqlAction, FixedSqlStreamingAction, FixedSqlAction }

import im.actor.server.models

import scala.util.matching.Regex.Groups

final class DialogTable(tag: Tag) extends Table[models.Dialog](tag, "dialogs") {

  def userId = column[Int]("user_id", O.PrimaryKey)

  def peerType = column[Int]("peer_type", O.PrimaryKey)

  def peerId = column[Int]("peer_id", O.PrimaryKey)

  def lastMessageDate = column[DateTime]("last_message_date")

  def lastReceivedAt = column[DateTime]("last_received_at")

  def lastReadAt = column[DateTime]("last_read_at")

  def ownerLastReceivedAt = column[DateTime]("owner_last_received_at")

  def ownerLastReadAt = column[DateTime]("owner_last_read_at")

  def isArchived = column[Boolean]("is_archived")

  def * = (userId, peerType, peerId, lastMessageDate, lastReceivedAt, lastReadAt, ownerLastReceivedAt, ownerLastReadAt, isArchived) <> (applyDialog.tupled, unapplyDialog)

  def applyDialog: (Int, Int, Int, DateTime, DateTime, DateTime, DateTime, DateTime, Boolean) ⇒ models.Dialog = {
    case (userId, peerType, peerId, lastMessageDate, lastReceivedAt, lastReadAt, ownerLastReceivedAt, ownerLastReadAt, isArchived) ⇒
      models.Dialog(
        userId = userId,
        peer = models.Peer(models.PeerType.fromInt(peerType), peerId),
        lastMessageDate = lastMessageDate,
        lastReceivedAt = lastReceivedAt,
        lastReadAt = lastReadAt,
        ownerLastReceivedAt = ownerLastReceivedAt,
        ownerLastReadAt = ownerLastReadAt,
        isArchived = isArchived
      )
  }

  def unapplyDialog: models.Dialog ⇒ Option[(Int, Int, Int, DateTime, DateTime, DateTime, DateTime, DateTime, Boolean)] = { dialog ⇒
    models.Dialog.unapply(dialog).map {
      case (userId, peer, lastMessageDate, lastReceivedAt, lastReadAt, ownerLastReceivedAt, ownerLastReadAt, isArchived) ⇒
        (userId, peer.typ.toInt, peer.id, lastMessageDate, lastReceivedAt, lastReadAt, ownerLastReceivedAt, ownerLastReadAt, isArchived)
    }
  }
}

object DialogRepo {
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

  val notHiddenDialogs = DialogRepo.dialogs joinLeft GroupRepo.groups on (_.peerId === _.id) filter {
    case (dialog, groupOpt) ⇒ dialog.isArchived === false && (groupOpt.map(!_.isHidden).getOrElse(true))
  } map (_._1)

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

  def findNotArchivedByUser(userId: Int, dateOpt: Option[DateTime], limit: Int)(implicit ec: ExecutionContext) = {
    val baseQuery = notHiddenDialogs
      .filter(d ⇒ d.userId === userId)
      .sortBy(_.lastMessageDate.desc)

    val limitedQuery = dateOpt match {
      case Some(date) ⇒ baseQuery.filter(_.lastMessageDate <= date)
      case None       ⇒ baseQuery
    }

    for {
      limited ← limitedQuery.take(limit).result
      // work-around for case when there are more than one dialog with the same lastMessageDate
      result ← limited
        .lastOption match {
          case Some(last) ⇒
            for {
              sameDate ← baseQuery.filter(_.lastMessageDate === last.lastMessageDate).result
            } yield limited.filterNot(_.lastMessageDate == last.lastMessageDate) ++ sameDate
          case None ⇒ DBIO.successful(limited)
        }
    } yield result
  }

  def updateLastMessageDate(userId: Int, peer: models.Peer, lastMessageDate: DateTime)(implicit ec: ExecutionContext) = {
    byPKC.applied((userId, peer.typ.toInt, peer.id)).map(_.lastMessageDate).update(lastMessageDate) flatMap {
      case 0 ⇒
        create(models.Dialog.withLastMessageDate(userId, peer, lastMessageDate))
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
          .map(userId ⇒ create(models.Dialog.withLastMessageDate(userId, peer, lastMessageDate)))
      )
    } yield userIds.size
  }

  def updateLastReceivedAt(userId: Int, peer: models.Peer, lastReceivedAt: DateTime)(implicit ec: ExecutionContext) = {
    byPKC.applied((userId, peer.typ.toInt, peer.id)).map(_.lastReceivedAt).update(lastReceivedAt) flatMap {
      case 0 ⇒
        create(models.Dialog.withLastReceivedAt(userId, peer, lastReceivedAt))
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
          .map(userId ⇒ create(models.Dialog.withLastReceivedAt(userId, peer, lastReceivedAt)))
      )
    } yield userIds.size
  }

  def updateOwnerLastReceivedAt(userId: Int, peer: models.Peer, ownerLastReceivedAt: DateTime)(implicit ec: ExecutionContext) = {
    byPKC.applied((userId, peer.typ.toInt, peer.id)).map(_.ownerLastReceivedAt).update(ownerLastReceivedAt) flatMap {
      case 0 ⇒
        create(models.Dialog.withOwnerLastReceivedAt(userId, peer, ownerLastReceivedAt))
      case x ⇒ DBIO.successful(x)
    }
  }

  def updateLastReadAt(userId: Int, peer: models.Peer, lastReadAt: DateTime)(implicit ec: ExecutionContext) = {
    byPKC.applied((userId, peer.typ.toInt, peer.id)).map(_.lastReadAt).update(lastReadAt) flatMap {
      case 0 ⇒
        create(models.Dialog.withLastReadAt(userId, peer, lastReadAt))
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
          .map(userId ⇒ create(models.Dialog.withLastReadAt(userId, peer, lastReadAt)))
      )
    } yield userIds.size
  }

  def updateOwnerLastReadAt(userId: Int, peer: models.Peer, ownerLastReadAt: DateTime)(implicit ec: ExecutionContext) = {
    byPKC.applied((userId, peer.typ.toInt, peer.id)).map(_.ownerLastReadAt).update(ownerLastReadAt) flatMap {
      case 0 ⇒
        create(models.Dialog.withOwnerLastReadAt(userId, peer, ownerLastReadAt))
      case x ⇒ DBIO.successful(x)
    }
  }

  def makeArchived(userId: Int, peer: models.Peer) =
    byPKC.applied((userId, peer.typ.toInt, peer.id)).map(_.isArchived).update(true)

  def delete(userId: Int, peer: models.Peer): FixedSqlAction[Int, NoStream, Write] =
    byPKC.applied((userId, peer.typ.toInt, peer.id)).delete
}
