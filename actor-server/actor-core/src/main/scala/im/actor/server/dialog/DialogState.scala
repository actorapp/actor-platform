package im.actor.server.dialog

import java.time.Instant

import akka.persistence.SnapshotMetadata
import im.actor.server.cqrs.{ Event, ProcessorState, TaggedEvent }
import im.actor.server.model.Peer

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
      if (x.randomId == y.randomId) 0
      else if (x.date.isBefore(y.date)) -1
      else if (x.date.isAfter(y.date)) 1
      else 0
  }
}

private case class UnreadMessage(date: Instant, randomId: Long)

private[dialog] object DialogState {
  def initial(userId: Int) = DialogState(
    userId = userId,
    lastMessageDate = Instant.ofEpochMilli(0),
    lastReceiveDate = Instant.ofEpochMilli(0),
    lastReadDate = Instant.ofEpochMilli(0),
    counter = 0,
    unreadMessages = SortedSet.empty(UnreadMessage.OrderingAsc),
    unreadMessagesMap = Map.empty
  )
}

private[dialog] final case class DialogState(
  userId:            Int,
  lastMessageDate:   Instant, //we don't use it now anywhere. should we remove it?
  lastReceiveDate:   Instant,
  lastReadDate:      Instant,
  counter:           Int,
  unreadMessages:    SortedSet[UnreadMessage],
  unreadMessagesMap: Map[Long, Long]
) extends ProcessorState[DialogState] {
  import DialogEvents._

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
      val readMessages = unreadMessages.takeWhile(um ⇒ um.date.isBefore(date) || um.date == date).map(_.randomId)
      val newUnreadMessages = unreadMessages.dropWhile(um ⇒ readMessages.contains(um.randomId))
      val newUnreadMessagesMap = unreadMessagesMap -- readMessages

      this.copy(
        counter = newUnreadMessages.size,
        unreadMessages = newUnreadMessages,
        unreadMessagesMap = newUnreadMessagesMap
      )
    case MessagesRead(date, readerUserId) if readerUserId != userId ⇒
      if (date.isAfter(lastReadDate))
        this.copy(lastReadDate = date)
      else this
    case MessagesReceived(date) ⇒
      if (date.isAfter(lastReceiveDate)) this.copy(lastReceiveDate = date)
      else this
    case CounterReset() ⇒
      this.copy(counter = 0, unreadMessages = SortedSet.empty(UnreadMessage.OrderingAsc), unreadMessagesMap = Map.empty)
    case Initialized() ⇒ this
  }

  override def withSnapshot(metadata: SnapshotMetadata, snapshot: Any): DialogState = snapshot match {
    case s: DialogStateSnapshot ⇒
      copy(
        userId = s.userId,
        lastMessageDate = s.lastMessageDate,
        lastReceiveDate = s.lastReceiveDate,
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
    lastReceiveDate = lastReceiveDate,
    lastReadDate = lastReadDate,
    counter = counter,
    unreadMessages = unreadMessagesMap
  )
}
