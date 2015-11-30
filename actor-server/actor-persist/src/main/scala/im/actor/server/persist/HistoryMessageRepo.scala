package im.actor.server.persist

import com.github.tototoshi.slick.PostgresJodaSupport._
import im.actor.server.model.{ Peer, PeerType, HistoryMessage }
import org.joda.time.DateTime
import slick.dbio.Effect.{ Write, Read }
import slick.driver.PostgresDriver
import slick.driver.PostgresDriver.api._
import slick.jdbc.GetResult
import slick.profile.{ SqlStreamingAction, SqlAction, FixedSqlStreamingAction, FixedSqlAction }

class HistoryMessageTable(tag: Tag) extends Table[HistoryMessage](tag, "history_messages") {
  def userId = column[Int]("user_id", O.PrimaryKey)

  def peerType = column[Int]("peer_type", O.PrimaryKey)

  def peerId = column[Int]("peer_id", O.PrimaryKey)

  def date = column[DateTime]("date", O.PrimaryKey)

  def senderUserId = column[Int]("sender_user_id", O.PrimaryKey)

  def randomId = column[Long]("random_id", O.PrimaryKey)

  def messageContentHeader = column[Int]("message_content_header")

  def messageContentData = column[Array[Byte]]("message_content_data")

  def deletedAt = column[Option[DateTime]]("deleted_at")

  def * = (userId, peerType, peerId, date, senderUserId, randomId, messageContentHeader, messageContentData, deletedAt) <>
    (applyHistoryMessage.tupled, unapplyHistoryMessage)

  private def applyHistoryMessage: (Int, Int, Int, DateTime, Int, Long, Int, Array[Byte], Option[DateTime]) ⇒ HistoryMessage = {
    case (userId, peerType, peerId, date, senderUserId, randomId, messageContentHeader, messageContentData, deletedAt) ⇒
      HistoryMessage(
        userId = userId,
        peer = Peer(PeerType.fromValue(peerType), peerId),
        date = date,
        senderUserId = senderUserId,
        randomId = randomId,
        messageContentHeader = messageContentHeader,
        messageContentData = messageContentData,
        deletedAt = deletedAt
      )
  }

  private def unapplyHistoryMessage: HistoryMessage ⇒ Option[(Int, Int, Int, DateTime, Int, Long, Int, Array[Byte], Option[DateTime])] = { historyMessage ⇒
    HistoryMessage.unapply(historyMessage) map {
      case (userId, peer, date, senderUserId, randomId, messageContentHeader, messageContentData, deletedAt) ⇒
        (userId, peer.typ.value, peer.id, date, senderUserId, randomId, messageContentHeader, messageContentData, deletedAt)
    }
  }
}

object HistoryMessageRepo {
  val messages = TableQuery[HistoryMessageTable]
  val messagesC = Compiled(messages)

  val notDeletedMessages = messages.filter(_.deletedAt.isEmpty)

  val withoutServiceMessages = notDeletedMessages.filter(_.messageContentHeader =!= 2)

  def create(message: HistoryMessage): FixedSqlAction[Int, NoStream, Write] =
    messagesC += message

  def create(newMessages: Seq[HistoryMessage]): FixedSqlAction[Option[Int], NoStream, Write] =
    messagesC ++= newMessages

  def find(userId: Int, peer: Peer, dateOpt: Option[DateTime], limit: Int): FixedSqlStreamingAction[Seq[HistoryMessage], HistoryMessage, Read] = {
    val baseQuery = notDeletedMessages
      .filter(m ⇒
        m.userId === userId &&
          m.peerType === peer.typ.value &&
          m.peerId === peer.id)

    val query = dateOpt match {
      case Some(date) ⇒
        baseQuery.filter(_.date <= date).sortBy(_.date.desc)
      case None ⇒
        baseQuery.sortBy(_.date.asc)
    }

    query.take(limit).result
  }

  def findBySender(senderUserId: Int, peer: Peer, randomId: Long): FixedSqlStreamingAction[Seq[HistoryMessage], HistoryMessage, Read] =
    notDeletedMessages.filter(m ⇒ m.senderUserId === senderUserId && m.peerType === peer.typ.value && m.peerId === peer.id && m.randomId === randomId).result

  def findUserIds(peer: Peer, randomIds: Set[Long]): DBIO[Seq[Int]] =
    notDeletedMessages
      .filter(m ⇒ m.peerType === peer.typ.value && m.peerId === peer.id && (m.randomId inSet randomIds))
      .map(_.userId)
      .result

  def findNewest(userId: Int, peer: Peer): SqlAction[Option[HistoryMessage], NoStream, Read] =
    notDeletedMessages
      .filter(m ⇒ m.userId === userId && m.peerType === peer.typ.value && m.peerId === peer.id)
      .sortBy(_.date.desc)
      .take(1)
      .result
      .headOption

  def find(userId: Int, peer: Peer): FixedSqlStreamingAction[Seq[HistoryMessage], HistoryMessage, Read] =
    notDeletedMessages
      .filter(m ⇒ m.userId === userId && m.peerType === peer.typ.value && m.peerId === peer.id)
      .sortBy(_.date.desc)
      .result

  def find(userId: Int, peer: Peer, randomIds: Set[Long]): FixedSqlStreamingAction[Seq[HistoryMessage], HistoryMessage, Read] =
    notDeletedMessages.filter(m ⇒ m.userId === userId && m.peerType === peer.typ.value && m.peerId === peer.id && (m.randomId inSet randomIds)).result

  def updateContentAll(userIds: Set[Int], randomId: Long, peerType: PeerType, peerIds: Set[Int],
                       messageContentHeader: Int, messageContentData: Array[Byte]): FixedSqlAction[Int, NoStream, Write] =
    notDeletedMessages
      .filter(m ⇒ m.randomId === randomId && m.peerType === peerType.value)
      .filter(_.peerId inSet peerIds)
      .filter(_.userId inSet userIds)
      .map(m ⇒ (m.messageContentHeader, m.messageContentData))
      .update((messageContentHeader, messageContentData))

  def getUnreadCount(userId: Int, peer: Peer, lastReadAt: DateTime, noServiceMessages: Boolean = false): FixedSqlAction[Int, PostgresDriver.api.NoStream, Read] =
    (if (noServiceMessages) withoutServiceMessages else notDeletedMessages)
      .filter(m ⇒ m.userId === userId && m.peerType === peer.typ.value && m.peerId === peer.id)
      .filter(m ⇒ m.date > lastReadAt && m.senderUserId =!= userId)
      .length
      .result

  def uniqueAsc(fromTs: Long, limit: Int): SqlStreamingAction[Vector[HistoryMessage], HistoryMessage, Effect] = {
    implicit val getMessageResult: GetResult[HistoryMessage] = GetResult(r ⇒
      HistoryMessage(
        userId = r.nextInt,
        peer = Peer(PeerType.fromValue(r.nextInt), r.nextInt),
        date = getDatetimeResult(r),
        senderUserId = r.nextInt,
        randomId = r.nextLong,
        messageContentHeader = r.nextInt,
        messageContentData = r.nextBytes,
        deletedAt = getDatetimeOptionResult(r)
      ))

    val serviceHeader = 2
    val date = new DateTime(fromTs)
    sql"""select distinct on (date, random_id) user_id, peer_type, peer_id, date, sender_user_id, random_id, message_content_header, message_content_data, deleted_at from history_messages
         where message_content_header != $serviceHeader
         and date > $date
         and deleted_at is null
         order by date asc, random_id asc
         limit $limit"""
      .as[HistoryMessage]
  }

  //возможно тут не учитываются
  private def unreadTotal(userId: Rep[Int]) =
    (for {
      d ← DialogRepo.notHiddenNotArchived.filter(_.userId === userId)
      m ← notDeletedMessages.filter(_.senderUserId =!= userId)
      if m.userId === d.userId && m.peerType === d.peerType && m.peerId === d.peerId && m.date > d.ownerLastReadAt
    } yield m.date).length

  private val unreadTotalC = Compiled(unreadTotal _)

  def getUnreadTotal(userId: Int): DBIO[Int] = unreadTotalC(userId).result

  def haveMessagesBetween(userId: Int, peer: Peer, minDate: DateTime, maxDate: DateTime) =
    notDeletedMessages
      .filter(m ⇒ m.userId === userId && m.peerType === peer.typ.value && m.peerId === peer.id)
      .filter(m ⇒ m.date > minDate && m.date < maxDate && m.senderUserId =!= userId)
      .exists
      .result

  def deleteAll(userId: Int, peer: Peer): FixedSqlAction[Int, NoStream, Write] =
    notDeletedMessages
      .filter(m ⇒ m.userId === userId && m.peerType === peer.typ.value && m.peerId === peer.id)
      .map(_.deletedAt)
      .update(Some(new DateTime))

  def delete(userId: Int, peer: Peer, randomIds: Set[Long]) =
    notDeletedMessages
      .filter(m ⇒ m.userId === userId && m.peerType === peer.typ.value && m.peerId === peer.id)
      .filter(_.randomId inSet randomIds)
      .map(_.deletedAt)
      .update(Some(new DateTime))
}