package im.actor.server.persist

import im.actor.server.models
import slick.driver.PostgresDriver.api._
import Database.dynamicSession
import org.joda.time.DateTime
import com.github.tototoshi.slick.PostgresJodaSupport._
import scodec.bits.BitVector

class DialogTable(tag: Tag) extends Table[models.Dialog](tag, "dialogs") {
  import MessageStateColumnType._

  def userId = column[Int]("user_id", O.PrimaryKey)
  def peerType = column[Int]("peer_type", O.PrimaryKey)
  def peerId = column[Int]("peer_id", O.PrimaryKey)
  def sortDate = column[DateTime]("sort_date")
  def senderUserId = column[Int]("sender_user_id")
  def randomId = column[Long]("random_id")
  def date = column[DateTime]("date")
  def messageContentHeader = column[Int]("message_content_header")
  def messageContentData = column[BitVector]("message_content_data")
  def state = column[models.MessageState]("state")

  def * = (userId, peerType, peerId, sortDate, senderUserId, randomId, date, messageContentHeader,
    messageContentData, state) <> (applyDialog, unapplyDialog)

  def applyDialog: ((Int, Int, Int, DateTime, Int, Long, DateTime, Int, BitVector, models.MessageState)) => models.Dialog = {
    case (userId, peerType, peerId, sortDate, senderUserId, randomId, date, mcHeader, mcData, state) =>
      models.Dialog(userId = userId,
        peer = models.Peer(models.PeerType.fromInt(peerType), peerId),
        sortDate = sortDate,
        senderUserId = senderUserId,
        randomId = randomId,
        date = date,
        messageContentHeader = mcHeader,
        messageContentData = mcData,
        state = state
      )
  }

  def unapplyDialog: models.Dialog => Option[(Int, Int, Int, DateTime, Int, Long, DateTime, Int, BitVector, models.MessageState)] = { dialog =>
    models.Dialog.unapply(dialog).map {
      case (userId, peer, sortDate, senderUserId, randomId, date, mcHeader, mcData, state) =>
        (userId, peer.typ.toInt, peer.id, sortDate, senderUserId, randomId, date, mcHeader, mcData, state)
    }
  }
}

object Dialog {
  val table = TableQuery[DialogTable]
}
