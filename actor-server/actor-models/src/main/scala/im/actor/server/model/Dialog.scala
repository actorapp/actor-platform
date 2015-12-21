package im.actor.server.model

import org.joda.time.DateTime

object Dialog {
  def apply(userId: Int, peer: Peer): Dialog =
    Dialog(userId, peer, new DateTime(0), new DateTime(0), new DateTime(0), new DateTime(0), new DateTime(0), Some(new DateTime), false, false, new DateTime)

  def withLastMessageDate(userId: Int, peer: Peer, lastMessageDate: DateTime) =
    Dialog(userId, peer, lastMessageDate, new DateTime(0), new DateTime(0), new DateTime(0), new DateTime(0), Some(new DateTime), false, false, new DateTime)

  def withLastReceivedAt(userId: Int, peer: Peer, lastReceivedAt: DateTime) =
    Dialog(userId, peer, new DateTime(0), lastReceivedAt, new DateTime(0), new DateTime(0), new DateTime(0), Some(new DateTime), false, false, new DateTime)

  def withOwnerLastReceivedAt(userId: Int, peer: Peer, ownerLastReceivedAt: DateTime) =
    Dialog(userId, peer, new DateTime(0), new DateTime(0), new DateTime(0), ownerLastReceivedAt, new DateTime(0), Some(new DateTime), false, false, new DateTime)

  def withLastReadAt(userId: Int, peer: Peer, lastReadAt: DateTime) =
    Dialog(userId, peer, new DateTime(0), new DateTime(0), lastReadAt, new DateTime(0), new DateTime(0), Some(new DateTime), false, false, new DateTime)

  def withOwnerLastReadAt(userId: Int, peer: Peer, ownerLastReadAt: DateTime) =
    Dialog(userId, peer, new DateTime(0), new DateTime(0), new DateTime(0), new DateTime(0), ownerLastReadAt, Some(new DateTime), false, false, new DateTime)
}

@SerialVersionUID(1L)
case class Dialog(
  userId:              Int,
  peer:                Peer,
  lastMessageDate:     DateTime,
  lastReceivedAt:      DateTime,
  lastReadAt:          DateTime,
  ownerLastReceivedAt: DateTime,
  ownerLastReadAt:     DateTime,
  shownAt:             Option[DateTime],
  isFavourite:         Boolean,
  isArchived:          Boolean,
  createdAt:           DateTime
)
