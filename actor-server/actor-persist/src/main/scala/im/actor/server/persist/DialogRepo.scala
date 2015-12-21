package im.actor.server.persist

import im.actor.server.model.{ Peer, Dialog, PeerType }
import slick.lifted.ColumnOrdered

import scala.concurrent.ExecutionContext

import com.github.tototoshi.slick.PostgresJodaSupport._
import org.joda.time.DateTime
import slick.dbio.Effect.{ Read, Write }
import slick.driver.PostgresDriver.api._
import slick.profile.{ SqlAction, FixedSqlStreamingAction, FixedSqlAction }

final class DialogTable(tag: Tag) extends Table[Dialog](tag, "dialogs") {

  def userId = column[Int]("user_id", O.PrimaryKey)

  def peerType = column[Int]("peer_type", O.PrimaryKey)

  def peerId = column[Int]("peer_id", O.PrimaryKey)

  def lastMessageDate = column[DateTime]("last_message_date")

  def lastReceivedAt = column[DateTime]("last_received_at")

  def lastReadAt = column[DateTime]("last_read_at")

  def ownerLastReceivedAt = column[DateTime]("owner_last_received_at")

  def ownerLastReadAt = column[DateTime]("owner_last_read_at")

  def shownAt = column[Option[DateTime]]("shown_at")

  def isFavourite = column[Boolean]("is_favourite")

  def isArchived = column[Boolean]("is_archived")

  def createdAt = column[DateTime]("created_at")

  def * = (
    userId,
    peerType,
    peerId,
    lastMessageDate,
    lastReceivedAt,
    lastReadAt,
    ownerLastReceivedAt,
    ownerLastReadAt,
    shownAt,
    isFavourite,
    isArchived,
    createdAt
  ) <> (applyDialog.tupled, unapplyDialog)

  def applyDialog: (Int, Int, Int, DateTime, DateTime, DateTime, DateTime, DateTime, Option[DateTime], Boolean, Boolean, DateTime) ⇒ Dialog = {
    case (
      userId,
      peerType,
      peerId,
      lastMessageDate,
      lastReceivedAt,
      lastReadAt,
      ownerLastReceivedAt,
      ownerLastReadAt,
      shownAt,
      isFavourite,
      isArchived,
      createdAt) ⇒
      Dialog(
        userId = userId,
        peer = Peer(PeerType.fromValue(peerType), peerId),
        lastMessageDate = lastMessageDate,
        lastReceivedAt = lastReceivedAt,
        lastReadAt = lastReadAt,
        ownerLastReceivedAt = ownerLastReceivedAt,
        ownerLastReadAt = ownerLastReadAt,
        shownAt = shownAt,
        isFavourite = isFavourite,
        isArchived = isArchived,
        createdAt = createdAt
      )
  }

  def unapplyDialog: Dialog ⇒ Option[(Int, Int, Int, DateTime, DateTime, DateTime, DateTime, DateTime, Option[DateTime], Boolean, Boolean, DateTime)] = { dialog ⇒
    Dialog.unapply(dialog).map {
      case (userId, peer, lastMessageDate, lastReceivedAt, lastReadAt, ownerLastReceivedAt, ownerLastReadAt, shownAt, isFavourite, isArchived, createdAt) ⇒
        (userId, peer.typ.value, peer.id, lastMessageDate, lastReceivedAt, lastReadAt, ownerLastReceivedAt, ownerLastReadAt, shownAt, isFavourite, isArchived, createdAt)
    }
  }
}

object DialogRepo {
  val dialogs = TableQuery[DialogTable]
  val dialogsC = Compiled(dialogs)

  def byPeerSimple(peerType: Rep[Int], peerId: Rep[Int]) =
    dialogs.filter(d ⇒ d.peerType === peerType && d.peerId === peerId)

  def byPKSimple(userId: Rep[Int], peerType: Rep[Int], peerId: Rep[Int]) =
    dialogs.filter(d ⇒ d.userId === userId && d.peerType === peerType && d.peerId === peerId)

  def byPK(userId: Int, peer: Peer) =
    byPKSimple(userId, peer.typ.value, peer.id)

  def byPeerType(userId: Rep[Int], peerType: Rep[Int]) =
    dialogs.filter(d ⇒ d.userId === userId && d.peerType === peerType)

  def idByPeerType(userId: Rep[Int], peerType: Rep[Int]) =
    byPeerType(userId, peerType).map(_.peerId)

  def userIdByPeerType(peerType: Rep[Int], peerId: Rep[Int]) =
    byPeerSimple(peerType, peerId)

  val byPKC = Compiled(byPKSimple _)
  val byPeerC = Compiled(byPeerSimple _)
  val byPeerTypeC = Compiled(byPeerType _)
  val idByPeerTypeC = Compiled(idByPeerType _)

  val notArchived = DialogRepo.dialogs joinLeft GroupRepo.groups on (_.peerId === _.id) filter {
    case (dialog, groupOpt) ⇒ dialog.isArchived === false && groupOpt.map(!_.isHidden).getOrElse(true)
  } map (_._1)

  val notHiddenNotArchived = notArchived filter (_.shownAt.isDefined)

  def create(dialog: Dialog) =
    dialogsC += dialog

  def create(dialogs: Seq[Dialog]) =
    dialogsC ++= dialogs

  def createIfNotExists(dialog: Dialog)(implicit ec: ExecutionContext): DBIO[Boolean] = {
    for {
      dOpt ← find(dialog.userId, dialog.peer)
      res ← if (dOpt.isEmpty) create(dialog).map(_ ⇒ true) else DBIO.successful(false)
    } yield res
  }

  def find(userId: Int, peer: Peer): SqlAction[Option[Dialog], NoStream, Read] =
    byPKC((userId, peer.typ.value, peer.id)).result.headOption

  def findGroups(userId: Int): FixedSqlStreamingAction[Seq[Dialog], Dialog, Read] =
    byPeerTypeC((userId, PeerType.Group.value)).result

  def findAllGroups(userIds: Set[Int], groupId: Int) =
    dialogs.filter(d ⇒ d.peerType === PeerType.Group.value && d.peerId === groupId && d.userId.inSet(userIds)).result

  def findGroupIds(userId: Int): FixedSqlStreamingAction[Seq[Int], Int, Read] =
    idByPeerTypeC((userId, PeerType.Group.value)).result

  def findUserIds(userId: Int): FixedSqlStreamingAction[Seq[Int], Int, Read] =
    idByPeerTypeC((userId, PeerType.Private.value)).result

  def findLastReadBefore(date: DateTime, userId: Int) =
    dialogs.filter(d ⇒ d.userId === userId && d.ownerLastReadAt < date).result

  def findNotArchivedSortByLastMessageData(userId: Int, dateOpt: Option[DateTime], limit: Int, fetchHidden: Boolean = false)(implicit ec: ExecutionContext): DBIO[Seq[Dialog]] =
    findNotArchived(userId, dateOpt: Option[DateTime], limit, _.shownAt.asc, fetchHidden)

  def findNotArchived(userId: Int, dateOpt: Option[DateTime], limit: Int, fetchHidden: Boolean = false)(implicit ec: ExecutionContext): DBIO[Seq[Dialog]] =
    findNotArchived(userId, dateOpt: Option[DateTime], limit, _.lastMessageDate.desc, fetchHidden)

  def findNotArchived[A](userId: Int, dateOpt: Option[DateTime], limit: Int, sortBy: DialogTable ⇒ ColumnOrdered[A], fetchHidden: Boolean)(implicit ec: ExecutionContext): DBIO[Seq[Dialog]] = {
    val baseQuery = (if (fetchHidden) notArchived else notHiddenNotArchived)
      .filter(d ⇒ d.userId === userId)
      .sortBy(sortBy)

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

  def hide(userId: Int, peer: Peer) =
    byPKC.applied((userId, peer.typ.value, peer.id)).map(_.shownAt).update(None)

  def show(userId: Int, peer: Peer) =
    byPKC.applied((userId, peer.typ.value, peer.id)).map(_.shownAt).update(Some(new DateTime))

  def favourite(userId: Int, peer: Peer) =
    byPKC.applied((userId, peer.typ.value, peer.id)).map(_.isFavourite).update(true)

  def unfavourite(userId: Int, peer: Peer) =
    byPKC.applied((userId, peer.typ.value, peer.id)).map(_.isFavourite).update(false)

  def updateLastMessageDate(userId: Int, peer: Peer, lastMessageDate: DateTime)(implicit ec: ExecutionContext) =
    byPKC.applied((userId, peer.typ.value, peer.id)).map(_.lastMessageDate).update(lastMessageDate)

  def findExistingUserIds(userIds: Set[Int], peer: Peer): FixedSqlStreamingAction[Seq[Int], Int, Read] = {
    byPeerC.applied((peer.typ.value, peer.id))
      .filter(_.userId inSetBind userIds)
      .map(_.userId)
      .result
  }

  def updateLastMessageDates(userIds: Set[Int], peer: Peer, lastMessageDate: DateTime)(implicit ec: ExecutionContext) = {
    byPeerC.applied((peer.typ.value, peer.id))
      .filter(_.userId inSetBind userIds)
      .map(_.lastMessageDate)
      .update(lastMessageDate)
  }

  def updateLastReceivedAt(userId: Int, peer: Peer, lastReceivedAt: DateTime)(implicit ec: ExecutionContext) =
    byPKC.applied((userId, peer.typ.value, peer.id)).map(_.lastReceivedAt).update(lastReceivedAt)

  def updateLastReceivedAt(userIds: Set[Int], peer: Peer, lastReceivedAt: DateTime)(implicit ec: ExecutionContext) = {
    byPeerC.applied((peer.typ.value, peer.id))
      .filter(_.userId inSetBind userIds)
      .map(_.lastReceivedAt)
      .update(lastReceivedAt)
  }

  def updateOwnerLastReceivedAt(userId: Int, peer: Peer, ownerLastReceivedAt: DateTime)(implicit ec: ExecutionContext) =
    byPKC.applied((userId, peer.typ.value, peer.id)).map(_.ownerLastReceivedAt).update(ownerLastReceivedAt)

  def updateLastReadAt(userId: Int, peer: Peer, lastReadAt: DateTime)(implicit ec: ExecutionContext) =
    byPKC.applied((userId, peer.typ.value, peer.id)).map(_.lastReadAt).update(lastReadAt)

  def updateLastReadAt(userIds: Set[Int], peer: Peer, lastReadAt: DateTime)(implicit ec: ExecutionContext) = {
    byPeerC.applied((peer.typ.value, peer.id))
      .filter(_.userId inSetBind userIds)
      .map(_.lastReadAt)
      .update(lastReadAt)
  }

  def updateOwnerLastReadAt(userId: Int, peer: Peer, ownerLastReadAt: DateTime)(implicit ec: ExecutionContext) =
    byPKC.applied((userId, peer.typ.value, peer.id)).map(_.ownerLastReadAt).update(ownerLastReadAt)

  def makeArchived(userId: Int, peer: Peer) =
    byPKC.applied((userId, peer.typ.value, peer.id)).map(_.isArchived).update(true)

  def delete(userId: Int, peer: Peer): FixedSqlAction[Int, NoStream, Write] =
    byPKC.applied((userId, peer.typ.value, peer.id)).delete
}
