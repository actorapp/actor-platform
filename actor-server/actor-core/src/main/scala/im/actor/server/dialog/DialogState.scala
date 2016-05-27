package im.actor.server.dialog

import java.time.Instant

import akka.event.{ Logging, LoggingAdapter }
import akka.persistence.SnapshotMetadata
import im.actor.server.cqrs.{ Event, ProcessorState, TaggedEvent }
import im.actor.server.model.Peer
import org.slf4j.LoggerFactory

import scala.collection.SortedSet

private[dialog] trait DialogEvent extends TaggedEvent {
  override def tags: Set[String] = Set("dialog")
}

trait DialogQuery {
  val dest: Option[Peer]

  def getDest: Peer
}

private object UnreadMessage {
  val OrderingAsc = new Ordering[UnreadMessage] {
    override def compare(x: UnreadMessage, y: UnreadMessage): Int =
      if (x.date.isBefore(y.date)) -1
      else if (x.date.isAfter(y.date)) 1
      else 0
  }
}

private case class UnreadMessage(date: Instant, randomId: Long)

private[dialog] object DialogState {
  def initial(userId: Int) = DialogState(
    userId = userId,
    lastMessageDate = Instant.ofEpochMilli(0),
    lastOwnerReceiveDate = Instant.ofEpochMilli(0),
    lastReceiveDate = Instant.ofEpochMilli(0),
    lastOwnerReadDate = Instant.ofEpochMilli(0),
    lastReadDate = Instant.ofEpochMilli(0),
    counter = 0,
    unreadMessages = SortedSet.empty(UnreadMessage.OrderingAsc),
    unreadMessagesMap = Map.empty
  )
}

private[dialog] final case class DialogState(
  userId:               Int,
  lastMessageDate:      Instant,
  lastOwnerReceiveDate: Instant,
  lastReceiveDate:      Instant,
  lastOwnerReadDate:    Instant,
  lastReadDate:         Instant,
  counter:              Int,
  unreadMessages:       SortedSet[UnreadMessage],
  unreadMessagesMap:    Map[Long, Long]
) extends ProcessorState[DialogState] {
  import DialogEvents._

  val log = LoggerFactory.getLogger(s"$userId/DialogRoot")

  override def updated(e: Event): DialogState = e match {
    case NewMessage(randomId, date, senderUserId, messageHeader) ⇒
      if (senderUserId != userId) {
        this.copy(
          counter = counter + 1,
          unreadMessages = unreadMessages + UnreadMessage(date, randomId),
          unreadMessagesMap = unreadMessagesMap + (randomId → date.toEpochMilli),
          lastMessageDate = date
        )
      } else this.copy(lastMessageDate = date)
    case MessagesRead(date, readerUserId) if readerUserId == userId ⇒
      log.debug(s"unreadMessages (fromState) ${unreadMessages}")
      val readMessages = unreadMessages.takeWhile(um ⇒ um.date.isBefore(date) || um.date == date)
      log.debug(s"readMessages ${readMessages}")
      log.debug(s"readMessages date ${unreadMessages.headOption map (um ⇒ um.date.isBefore(date) || um.date == date)}")
      val newUnreadMessages = unreadMessages.drop(readMessages.size)
      val newUnreadMessagesMap = unreadMessagesMap -- readMessages.map(_.randomId)

      this.copy(
        counter = newUnreadMessages.size,
        unreadMessages = newUnreadMessages,
        unreadMessagesMap = newUnreadMessagesMap,
        lastOwnerReadDate = date
      )
    case MessagesRead(date, readerUserId) if readerUserId != userId ⇒
      if (date.isBefore(Instant.now().plusMillis(1)) && (date.isAfter(lastReadDate) || date == lastReadDate)) // what's a point of creating state with same lastReadDate(last condition)
        this.copy(lastReadDate = date)
      else this
    case MessagesReceived(date, receiverUserId) if receiverUserId == userId ⇒
      this.copy(lastOwnerReceiveDate = date)
    case MessagesReceived(date, receiverUserId) if receiverUserId != userId ⇒
      if (date.isBefore(Instant.now().plusMillis(1)) && (date.isAfter(lastReceiveDate) || date == lastReceiveDate)) // what's a point of creating state with same lastReadDate(last condition)
        this.copy(lastReceiveDate = date)
      else this
    case SetCounter(newCounter) ⇒
      this.copy(counter = newCounter)
    case Initialized() ⇒ this
  }

  override def withSnapshot(metadata: SnapshotMetadata, snapshot: Any): DialogState = snapshot match {
    case s: DialogStateSnapshot ⇒
      copy(
        userId = s.userId,
        lastMessageDate = s.lastMessageDate,
        lastOwnerReceiveDate = s.lastOwnerReceiveDate,
        lastReceiveDate = s.lastReceiveDate,
        lastOwnerReadDate = s.lastOwnerReadDate,
        lastReadDate = s.lastReadDate,
        counter = s.counter,
        unreadMessages = SortedSet(
          (s.unreadMessages.toSeq map {
            case (randomId, ts) ⇒ UnreadMessage(Instant.ofEpochMilli(ts), randomId)
          }): _*
        )(UnreadMessage.OrderingAsc),
        unreadMessagesMap = s.unreadMessages
      )
  }

  override lazy val snapshot = DialogStateSnapshot(
    userId = userId,
    lastMessageDate = lastMessageDate,
    lastOwnerReceiveDate = lastOwnerReceiveDate,
    lastReceiveDate = lastReceiveDate,
    lastOwnerReadDate = lastOwnerReadDate,
    lastReadDate = lastReadDate,
    counter = counter,
    unreadMessages = unreadMessagesMap
  )

  private[dialog] def nextDate: Instant = {
    val now = Instant.now()
    if (unreadMessages.lastOption.exists(_.date == now)) now.plusMillis(1L)
    else now
  }
}
