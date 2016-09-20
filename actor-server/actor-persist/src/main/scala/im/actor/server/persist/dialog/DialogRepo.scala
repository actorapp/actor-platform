package im.actor.server.persist.dialog

import com.github.tototoshi.slick.PostgresJodaSupport._
import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model._
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext

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

  private val byUserC = Compiled(byUserId _)

  private def byPKSimple(userId: Rep[Int], peerType: Rep[Int], peerId: Rep[Int]) =
    dialogs.filter({ case (_, u) ⇒ u.userId === userId && u.peerType === peerType && u.peerId === peerId })

  private def byUserId(userId: Rep[Int]) =
    dialogs.filter({ case (_, u) ⇒ u.userId === userId })

  @deprecated("Migrations only", "2016-09-02")
  def findDialog(userId: Int, peer: Peer)(implicit ec: ExecutionContext): DBIO[Option[DialogObsolete]] =
    byPKC((userId, peer.typ.value, peer.id)).result.headOption map (_.map { case (c, u) ⇒ DialogObsolete.fromCommonAndUser(c, u) })

  @deprecated("Migrations only", "2016-09-02")
  def fetchDialogs(userId: Int)(implicit ec: ExecutionContext): DBIO[Seq[DialogObsolete]] =
    byUserC(userId).result map (_.map { case (c, u) ⇒ DialogObsolete.fromCommonAndUser(c, u) })
}
