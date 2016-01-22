package im.actor.server.persist.dialog

import com.github.tototoshi.slick.PostgresJodaSupport._
import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model._
import im.actor.server.persist.GroupRepo
import org.joda.time.DateTime
import slick.lifted.ColumnOrdered

import scala.concurrent.ExecutionContext

final class DialogCommonTable(tag: Tag) extends Table[DialogCommon](tag, "dialog_commons") {

  def dialogId = column[String]("dialog_id", O.PrimaryKey)

  def lastMessageDate = column[DateTime]("last_message_date")

  def lastReceivedAt = column[DateTime]("last_received_at")

  def lastReadAt = column[DateTime]("last_read_at")

  def createdAt = column[DateTime]("created_at")

  def * = (dialogId, lastMessageDate, lastReceivedAt, lastReadAt, createdAt) <> (applyDialogCommon.tupled, unapplyDialogCommon)

  def applyDialogCommon: (String, DateTime, DateTime, DateTime, DateTime) ⇒ DialogCommon = {
    case (
      dialogId,
      lastMessageDate,
      lastReceivedAt,
      lastReadAt,
      createdAt) ⇒
      DialogCommon(
        dialogId = dialogId,
        lastMessageDate = lastMessageDate,
        lastReceivedAt = lastReceivedAt,
        lastReadAt = lastReadAt,
        createdAt = createdAt
      )
  }

  def unapplyDialogCommon: DialogCommon ⇒ Option[(String, DateTime, DateTime, DateTime, DateTime)] = { dc ⇒
    DialogCommon.unapply(dc).map {
      case (dialogId, lastMessageDate, lastReceivedAt, lastReadAt, createdAt) ⇒
        (dialogId, lastMessageDate, lastReceivedAt, lastReadAt, createdAt)
    }
  }
}

final class UserDialogTable(tag: Tag) extends Table[UserDialog](tag, "user_dialogs") {

  def userId = column[Int]("user_id", O.PrimaryKey)

  def peerType = column[Int]("peer_type", O.PrimaryKey)

  def peerId = column[Int]("peer_id", O.PrimaryKey)

  def ownerLastReceivedAt = column[DateTime]("owner_last_received_at")

  def ownerLastReadAt = column[DateTime]("owner_last_read_at")

  def shownAt = column[Option[DateTime]]("shown_at")

  def isFavourite = column[Boolean]("is_favourite")

  def isArchived = column[Boolean]("is_archived")

  def * = (
    userId,
    peerType,
    peerId,
    ownerLastReceivedAt,
    ownerLastReadAt,
    shownAt,
    isFavourite,
    isArchived
  ) <> (applyUserDialog.tupled, unapplyUserDialog)

  def applyUserDialog: (Int, Int, Int, DateTime, DateTime, Option[DateTime], Boolean, Boolean) ⇒ UserDialog = {
    case (
      userId,
      peerType,
      peerId,
      ownerLastReceivedAt,
      ownerLastReadAt,
      shownAt,
      isFavourite,
      isArchived) ⇒
      UserDialog(
        userId = userId,
        peer = Peer(PeerType.fromValue(peerType), peerId),
        ownerLastReceivedAt = ownerLastReceivedAt,
        ownerLastReadAt = ownerLastReadAt,
        shownAt = shownAt,
        isFavourite = isFavourite,
        isArchived = isArchived
      )
  }

  def unapplyUserDialog: UserDialog ⇒ Option[(Int, Int, Int, DateTime, DateTime, Option[DateTime], Boolean, Boolean)] = { du ⇒
    UserDialog.unapply(du).map {
      case (userId, peer, ownerLastReceivedAt, ownerLastReadAt, shownAt, isFavourite, isArchived) ⇒
        (userId, peer.typ.value, peer.id, ownerLastReceivedAt, ownerLastReadAt, shownAt, isFavourite, isArchived)
    }
  }
}

object DialogRepo extends UserDialogOperations with DialogCommonOperations {

  val dialogs = for {
    c ← DialogCommonRepo.dialogCommon
    u ← UserDialogRepo.userDialogs if c.dialogId === repDialogId(u.userId, u.peerId, u.peerType)
  } yield (c, u)

  val byPKC = Compiled(byPKSimple _)

  val notArchived = DialogRepo.dialogs joinLeft GroupRepo.groups on (_._2.peerId === _.id) filter {
    case ((_, users), groupOpt) ⇒ users.isArchived === false && groupOpt.map(!_.isHidden).getOrElse(true)
  } map (_._1)

  val notHiddenNotArchived = notArchived filter { case (_, users) ⇒ users.shownAt.isDefined }

  private def byPKSimple(userId: Rep[Int], peerType: Rep[Int], peerId: Rep[Int]) =
    dialogs.filter({ case (_, u) ⇒ u.userId === userId && u.peerType === peerType && u.peerId === peerId })

  def create(dialog: Dialog)(implicit ec: ExecutionContext): DBIO[Int] = {
    val dialogId = getDialogId(Some(dialog.userId), dialog.peer)

    val common = DialogCommon(
      dialogId = dialogId,
      lastMessageDate = dialog.lastMessageDate,
      lastReceivedAt = dialog.lastReceivedAt,
      lastReadAt = dialog.lastReadAt,
      createdAt = dialog.createdAt
    )

    val user = UserDialog(
      userId = dialog.userId,
      peer = dialog.peer,
      ownerLastReceivedAt = dialog.ownerLastReceivedAt,
      ownerLastReadAt = dialog.ownerLastReadAt,
      shownAt = dialog.shownAt,
      isFavourite = dialog.isFavourite,
      isArchived = dialog.isArchived
    )

    for {
      exists ← commonExists(dialogId)
      result ← if (exists) {
        UserDialogRepo.userDialogs += user
      } else {
        for {
          c ← DialogCommonRepo.dialogCommon += common
          _ ← UserDialogRepo.userDialogs += user
        } yield c
      }
    } yield result
  }

  def findDialog(userId: Int, peer: Peer)(implicit ec: ExecutionContext): DBIO[Option[Dialog]] =
    byPKC((userId, peer.typ.value, peer.id)).result.headOption map (_.map { case (c, u) ⇒ Dialog.fromCommonAndUser(c, u) })

  def findNotArchivedSortByLastMessageData(userId: Int, dateOpt: Option[DateTime], limit: Int, fetchHidden: Boolean = false)(implicit ec: ExecutionContext): DBIO[Seq[Dialog]] =
    findNotArchived(userId, dateOpt: Option[DateTime], limit, { case (_, u) ⇒ u.shownAt.asc }, fetchHidden)

  def findNotArchived(userId: Int, dateOpt: Option[DateTime], limit: Int, fetchHidden: Boolean = false)(implicit ec: ExecutionContext): DBIO[Seq[Dialog]] =
    findNotArchived(userId, dateOpt: Option[DateTime], limit, { case (c, _) ⇒ c.lastMessageDate.desc }, fetchHidden)

  def findNotArchived[A](userId: Int, dateOpt: Option[DateTime], limit: Int, sorting: ((DialogCommonTable, UserDialogTable)) ⇒ ColumnOrdered[A], fetchHidden: Boolean)(implicit ec: ExecutionContext): DBIO[Seq[Dialog]] = {
    val baseQuery: Query[(DialogCommonTable, UserDialogTable), (DialogCommon, UserDialog), Seq] = (if (fetchHidden) notArchived else notHiddenNotArchived)
      .filter({ case (_, u) ⇒ u.userId === userId })
      .sortBy(sorting)

    val limitedQuery = dateOpt match {
      case Some(date) ⇒ baseQuery.filter({ case (c, _) ⇒ c.lastMessageDate <= date })
      case None       ⇒ baseQuery
    }

    for {
      limited ← limitedQuery.take(limit).result
      // work-around for case when there are more than one dialog with the same lastMessageDate
      result ← limited
        .lastOption match {
          case Some((last, _)) ⇒
            for {
              sameDate ← baseQuery.filter({ case (c, _) ⇒ c.lastMessageDate === last.lastMessageDate }).result
            } yield limited.filterNot({ case (c, _) ⇒ c.lastMessageDate == last.lastMessageDate }) ++ sameDate
          case None ⇒ DBIO.successful(limited)
        }
      dialogs = result map { case (c, u) ⇒ Dialog.fromCommonAndUser(c, u) }
    } yield dialogs
  }
}