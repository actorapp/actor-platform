package im.actor.server.dialog

import java.time.Instant

import akka.persistence.SnapshotMetadata
import im.actor.server.cqrs.{ Event, ProcessorState, TaggedEvent }
import im.actor.server.model.Peer
import org.slf4j.LoggerFactory

private[dialog] trait DialogEvent extends TaggedEvent {
  override def tags: Set[String] = Set("dialog")
}

trait DialogQuery {
  val dest: Option[Peer]

  def getDest: Peer
}

private[dialog] object DialogState {
  def initial(userId: Int) = DialogState(
    userId = userId,
    lastMessageDate = 0L,
    lastOwnerReceiveDate = 0L,
    lastReceiveDate = 0L,
    lastOwnerReadDate = 0L,
    lastReadDate = 0L,
    counter = 0,
    unreadTimestamps = Vector.empty
  )
}

private[dialog] final case class DialogState(
  userId:               Int,
  lastMessageDate:      Long,
  lastOwnerReceiveDate: Long,
  lastReceiveDate:      Long,
  lastOwnerReadDate:    Long,
  lastReadDate:         Long,
  counter:              Int,
  //TODO: не держать  количество непрочитанных в счетчике;
  // если коллекция unreadMessages становится
  // слишком большой, то нужно дропать старые элементы. если придет прочитка с старой датой - мы лезем
  // в базу за тем сообщением, читаем, и считаем сколько осталось непрочитанных
  // TODO: maybe we should use bounded(!) Vector/Queue/Stack and sort it manually?
  //TODO: сделать ограничение
  unreadTimestamps: Vector[Long]
) extends ProcessorState[DialogState] {
  import DialogEvents._

  val log = LoggerFactory.getLogger(s"$userId/DialogRoot")

  override def updated(e: Event): DialogState = e match {
    case NewMessage(randomId, dateMillis, senderUserId, messageHeader) ⇒
      // new message from peer
      if (senderUserId != userId) {
        this.copy(
          counter = counter + 1,
          unreadTimestamps =
          (if (unreadTimestamps.length >= DialogProcessor.MaxUnreadInState) unreadTimestamps.tail
          else unreadTimestamps) :+ dateMillis,
          lastMessageDate = dateMillis
        )
      } else {
        // new own message
        this.copy(lastMessageDate = dateMillis)
      }

    // все прочитки у нас за пределами unreadMessagesTimestamp, значит менять его не нужно?
    case UnreadsUpdated(newReadDate, newCounter) ⇒
      this.copy(
        counter = newCounter,
        lastOwnerReadDate = newReadDate
      )
    // user reads dialog with somebody. need to change
    case MessagesRead(dateMillis, readerUserId) if readerUserId == userId ⇒
      log.debug(s"unreadMessages (fromState) ${unreadTimestamps}")
      val newUnreadTimestamps = {
        val unreadsSorted = unreadTimestamps.sorted
        val readMessages = unreadsSorted.takeWhile(messDate ⇒ messDate <= dateMillis)
        log.debug(s"readMessages ${readMessages}")
        unreadsSorted.drop(readMessages.length)
      }
      this.copy(
        counter = newUnreadTimestamps.length,
        unreadTimestamps = newUnreadTimestamps,
        lastOwnerReadDate = dateMillis
      )
    // peer reads dialog with user. we need to update lastReadDate if it is newer than it was
    case MessagesRead(dateMillis, readerUserId) if readerUserId != userId ⇒
      if (dateMillis <= Instant.now.toEpochMilli && dateMillis > lastReadDate)
        this.copy(lastReadDate = dateMillis)
      else this
    case MessagesReceived(dateMillis, receiverUserId) if receiverUserId == userId ⇒
      this.copy(lastOwnerReceiveDate = dateMillis)
    case MessagesReceived(dateMillis, receiverUserId) if receiverUserId != userId ⇒
      if (dateMillis <= Instant.now.toEpochMilli && dateMillis > lastReceiveDate)
        this.copy(lastReceiveDate = dateMillis)
      else this
    case SetCounter(newCounter) ⇒
      this.copy(counter = newCounter)
    case Initialized() ⇒ this
  }

  override def withSnapshot(metadata: SnapshotMetadata, snapshot: Any): DialogState = snapshot match {
    case s: DialogStateSnapshot ⇒
      // compat with old snapshots
      //TODO: Vector
      val unreadTs: Seq[Long] =
        if (s.unreadMessagesObsolete.nonEmpty)
          s.unreadMessagesObsolete.keys.toSeq
        else s.unreadMessagesTimestamp
      copy(
        userId = s.userId,
        lastMessageDate = s.lastMessageDate,
        lastOwnerReceiveDate = s.lastOwnerReceiveDate,
        lastReceiveDate = s.lastReceiveDate,
        lastOwnerReadDate = s.lastOwnerReadDate,
        lastReadDate = s.lastReadDate,
        counter = s.counter,
        unreadTimestamps = unreadTs.toVector.sorted
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
    unreadMessagesTimestamp = unreadTimestamps,

    //compat with old snapshots
    unreadMessagesObsolete = Map.empty
  )

  // I guess we can remove this step completely, if we use Vector[Int] for unreadMessages
  private[dialog] def nextDate: Long = {
    val now = Instant.now.toEpochMilli
    if (unreadTimestamps.lastOption.contains(now)) now + 1L
    else now
  }
}
