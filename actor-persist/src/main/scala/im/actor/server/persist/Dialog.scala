package im.actor.server.persist

import scala.concurrent.ExecutionContext

import com.github.tototoshi.slick.PostgresJodaSupport._
import org.joda.time.DateTime
import scodec.bits.BitVector
import slick.driver.PostgresDriver.api._

import im.actor.server.models

class DialogTable(tag: Tag) extends Table[models.Dialog](tag, "dialogs") {

  import MessageStateColumnType._

  def userId = column[Int]("user_id", O.PrimaryKey)

  def peerType = column[Int]("peer_type", O.PrimaryKey)

  def peerId = column[Int]("peer_id", O.PrimaryKey)

  def lastMessageDate = column[DateTime]("last_message_date")

  def lastReceivedAt = column[DateTime]("last_received_at")

  def lastReadAt = column[DateTime]("last_read_at")

  def * = (userId, peerType, peerId, lastMessageDate, lastReceivedAt, lastReadAt) <>(applyDialog.tupled, unapplyDialog)

  def applyDialog: (Int, Int, Int, DateTime, DateTime, DateTime) => models.Dialog = {
    case (userId, peerType, peerId, lastMessageDate, lastReceivedAt, lastReadAt) =>
      models.Dialog(
        userId = userId,
        peer = models.Peer(models.PeerType.fromInt(peerType), peerId),
        lastMessageDate = lastMessageDate,
        lastReceivedAt = lastReceivedAt,
        lastReadAt = lastReadAt
      )
  }

  def unapplyDialog: models.Dialog => Option[(Int, Int, Int, DateTime, DateTime, DateTime)] = { dialog =>
    models.Dialog.unapply(dialog).map {
      case (userId, peer, lastMessageDate, lastReceivedAt, lastReadAt) =>
        (userId, peer.typ.toInt, peer.id, lastMessageDate, lastReceivedAt, lastReadAt)
    }
  }
}

object Dialog {
  val dialogs = TableQuery[DialogTable]

  def byUserIdPeer(userId: Int, peer: models.Peer) =
    dialogs.filter(d => d.userId === userId && d.peerType === peer.typ.toInt && d.peerId === peer.id)

  def create(dialog: models.Dialog) =
    dialogs += dialog

  def createIfNotExists(dialog: models.Dialog)(implicit ec: ExecutionContext) = {
    for {
      dOpt <- find(dialog.userId, dialog.peer)
      res <- if (dOpt.isEmpty) create(dialog) else DBIO.successful(0)
    } yield res
  }

  def find(userId: Int, peer: models.Peer) =
    dialogs.filter(d => d.userId === userId && d.peerType === peer.typ.toInt && d.peerId === peer.id).result

  def updateLastMessageDate(userId: Int, peer: models.Peer, lastMessageDate: DateTime)
                           (implicit ec: ExecutionContext) = {
    byUserIdPeer(userId, peer).map(_.lastMessageDate).update(lastMessageDate) flatMap {
      case 0 =>
        create(models.Dialog(userId, peer, lastMessageDate, new DateTime(0), new DateTime(0)))
      case x => DBIO.successful(x)
    }
  }
}
