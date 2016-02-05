package im.actor.server.persist.dialog

import com.github.tototoshi.slick.PostgresJodaSupport._
import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model._
import im.actor.server.persist.GroupRepo
import org.joda.time.DateTime
import slick.lifted.ColumnOrdered

import scala.concurrent.ExecutionContext
import scala.util.{ Success, Failure }

final class DialogCommonTable(tag: Tag) extends Table[DialogCommon](tag, "dialog_commons") {

  def dialogId = column[String]("dialog_id", O.PrimaryKey)

  def lastMessageDate = column[DateTime]("last_message_date")

  def lastReceivedAt = column[DateTime]("last_received_at")

  def lastReadAt = column[DateTime]("last_read_at")

  def * = (dialogId, lastMessageDate, lastReceivedAt, lastReadAt) <> (applyDialogCommon.tupled, unapplyDialogCommon)

  def applyDialogCommon: (String, DateTime, DateTime, DateTime) ⇒ DialogCommon = {
    case (
      dialogId,
      lastMessageDate,
      lastReceivedAt,
      lastReadAt) ⇒
      DialogCommon(
        dialogId = dialogId,
        lastMessageDate = lastMessageDate,
        lastReceivedAt = lastReceivedAt,
        lastReadAt = lastReadAt
      )
  }

  def unapplyDialogCommon: DialogCommon ⇒ Option[(String, DateTime, DateTime, DateTime)] = { dc ⇒
    DialogCommon.unapply(dc).map {
      case (dialogId, lastMessageDate, lastReceivedAt, lastReadAt) ⇒
        (dialogId, lastMessageDate, lastReceivedAt, lastReadAt)
    }
  }
}

final class UserDialogTable(tag: Tag) extends Table[UserDialog](tag, "user_dialogs") {

  def userId = column[Int]("user_id", O.PrimaryKey)

  def peerType = column[Int]("peer_type", O.PrimaryKey)

  def peerId = column[Int]("peer_id", O.PrimaryKey)

  def ownerLastReceivedAt = column[DateTime]("owner_last_received_at")

  def ownerLastReadAt = column[DateTime]("owner_last_read_at")

  def createdAt = column[DateTime]("created_at")

  def shownAt = column[Option[DateTime]]("shown_at")

  def isFavourite = column[Boolean]("is_favourite")

  def archivedAt = column[Option[DateTime]]("archived_at")

  def * = (
    userId,
    peerType,
    peerId,
    ownerLastReceivedAt,
    ownerLastReadAt,
    createdAt,
    shownAt,
    isFavourite,
    archivedAt
  ) <> (applyUserDialog.tupled, unapplyUserDialog)

  def applyUserDialog: (Int, Int, Int, DateTime, DateTime, DateTime, Option[DateTime], Boolean, Option[DateTime]) ⇒ UserDialog = {
    case (
      userId,
      peerType,
      peerId,
      ownerLastReceivedAt,
      ownerLastReadAt,
      createdAt,
      shownAt,
      isFavourite,
      archivedAt) ⇒
      UserDialog(
        userId = userId,
        peer = Peer(PeerType.fromValue(peerType), peerId),
        ownerLastReceivedAt = ownerLastReceivedAt,
        ownerLastReadAt = ownerLastReadAt,
        createdAt = createdAt,
        shownAt = shownAt,
        isFavourite = isFavourite,
        archivedAt = archivedAt
      )
  }

  def unapplyUserDialog: UserDialog ⇒ Option[(Int, Int, Int, DateTime, DateTime, DateTime, Option[DateTime], Boolean, Option[DateTime])] = { du ⇒
    UserDialog.unapply(du).map {
      case (userId, peer, ownerLastReceivedAt, ownerLastReadAt, createdAt, shownAt, isFavourite, archivedAt) ⇒
        (userId, peer.typ.value, peer.id, ownerLastReceivedAt, ownerLastReadAt, createdAt, shownAt, isFavourite, archivedAt)
    }
  }
}

object DialogRepo extends UserDialogOperations with DialogCommonOperations {

  private val dialogs = for {
    c ← DialogCommonRepo.dialogCommon
    u ← UserDialogRepo.userDialogs if c.dialogId === repDialogId(u.userId, u.peerId, u.peerType)
  } yield (c, u)

  private val byPKC = Compiled(byPKSimple _)

  private val archived = DialogRepo.dialogs.filter(_._2.archivedAt.isDefined)

  private def archivedByUserId(
    userId: Rep[Int],
    offset: ConstColumn[Long],
    limit:  ConstColumn[Long]
  ) = archived filter (_._2.userId === userId) drop offset take limit

  private val archivedByUserIdC = Compiled(archivedByUserId _)

  private val archivedExistC = Compiled { (userId: Rep[Int]) ⇒
    archivedByUserId(userId, 0L, 1L).take(1).exists
  }

  private def byPKSimple(userId: Rep[Int], peerType: Rep[Int], peerId: Rep[Int]) =
    dialogs.filter({ case (_, u) ⇒ u.userId === userId && u.peerType === peerType && u.peerId === peerId })

  def create(dialog: Dialog)(implicit ec: ExecutionContext): DBIO[Int] = {
    val dialogId = getDialogId(Some(dialog.userId), dialog.peer)

    val common = DialogCommon(
      dialogId = dialogId,
      lastMessageDate = dialog.lastMessageDate,
      lastReceivedAt = dialog.lastReceivedAt,
      lastReadAt = dialog.lastReadAt
    )

    val user = UserDialog(
      userId = dialog.userId,
      peer = dialog.peer,
      ownerLastReceivedAt = dialog.ownerLastReceivedAt,
      ownerLastReadAt = dialog.ownerLastReadAt,
      createdAt = dialog.createdAt,
      shownAt = dialog.shownAt,
      isFavourite = dialog.isFavourite,
      archivedAt = dialog.archivedAt
    )

    for {
      exists ← commonExists(dialogId)
      result ← if (exists) {
        UserDialogRepo.userDialogs += user
      } else {
        for {
          c ← (DialogCommonRepo.dialogCommon += common)
            .asTry
            .flatMap {
              case Failure(e) ⇒
                commonExists(common.dialogId) flatMap {
                  case true  ⇒ DBIO.successful(1)
                  case false ⇒ DBIO.failed(e)
                }
              case Success(res) ⇒ DBIO.successful(res)
            }
          _ ← UserDialogRepo.userDialogs += user
        } yield c
      }
    } yield result
  }

  def findDialog(userId: Int, peer: Peer)(implicit ec: ExecutionContext): DBIO[Option[Dialog]] =
    byPKC((userId, peer.typ.value, peer.id)).result.headOption map (_.map { case (c, u) ⇒ Dialog.fromCommonAndUser(c, u) })

  def fetchSortByLastMessageData(userId: Int, dateOpt: Option[DateTime], limit: Int, fetchArchived: Boolean = false)(implicit ec: ExecutionContext): DBIO[Seq[Dialog]] =
    fetch(userId, dateOpt: Option[DateTime], limit, { case (_, u) ⇒ u.shownAt.asc }, fetchArchived)

  def fetch(userId: Int, dateOpt: Option[DateTime], limit: Int, fetchArchived: Boolean = false)(implicit ec: ExecutionContext): DBIO[Seq[Dialog]] =
    fetch(userId, dateOpt: Option[DateTime], limit, { case (c, _) ⇒ c.lastMessageDate.desc }, fetchArchived)

  def fetch[A](userId: Int, dateOpt: Option[DateTime], limit: Int, sorting: ((DialogCommonTable, UserDialogTable)) ⇒ ColumnOrdered[A], fetchArchived: Boolean)(implicit ec: ExecutionContext): DBIO[Seq[Dialog]] = {
    val baseQuery: Query[(DialogCommonTable, UserDialogTable), (DialogCommon, UserDialog), Seq] =
      (if (fetchArchived) archived else dialogs)
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

  def archivedExist(userId: Int) = archivedExistC(userId).result

  def fetchArchived(userId: Int, offset: Int, limit: Int)(implicit ec: ExecutionContext) =
    archivedByUserIdC((userId, offset.toLong, limit.toLong)).result map (_ map { case (c, u) ⇒ Dialog.fromCommonAndUser(c, u) })
}