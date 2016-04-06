package im.actor.server.model

import org.joda.time.DateTime

object DialogObsolete {
  def apply(userId: Int, peer: Peer): DialogObsolete =
    DialogObsolete(userId, peer, new DateTime(0), new DateTime(0), new DateTime(0), new DateTime(0), new DateTime(0), Some(new DateTime), false, None, new DateTime)

  def withLastMessageDate(userId: Int, peer: Peer, lastMessageDate: DateTime) =
    DialogObsolete(userId, peer, lastMessageDate, new DateTime(0), new DateTime(0), new DateTime(0), new DateTime(0), Some(new DateTime), false, None, new DateTime)

  def withLastReceivedAt(userId: Int, peer: Peer, lastReceivedAt: DateTime) =
    DialogObsolete(userId, peer, new DateTime(0), lastReceivedAt, new DateTime(0), new DateTime(0), new DateTime(0), Some(new DateTime), false, None, new DateTime)

  def withOwnerLastReceivedAt(userId: Int, peer: Peer, ownerLastReceivedAt: DateTime) =
    DialogObsolete(userId, peer, new DateTime(0), new DateTime(0), new DateTime(0), ownerLastReceivedAt, new DateTime(0), Some(new DateTime), false, None, new DateTime)

  def withLastReadAt(userId: Int, peer: Peer, lastReadAt: DateTime) =
    DialogObsolete(userId, peer, new DateTime(0), new DateTime(0), lastReadAt, new DateTime(0), new DateTime(0), Some(new DateTime), false, None, new DateTime)

  def withOwnerLastReadAt(userId: Int, peer: Peer, ownerLastReadAt: DateTime) =
    DialogObsolete(userId, peer, new DateTime(0), new DateTime(0), new DateTime(0), new DateTime(0), ownerLastReadAt, Some(new DateTime), false, None, new DateTime)

  def fromCommonAndUser(c: DialogCommon, u: UserDialog) = DialogObsolete(
    userId = u.userId,
    peer = u.peer,
    lastMessageDate = c.lastMessageDate,
    lastReceivedAt = c.lastReceivedAt,
    lastReadAt = c.lastReadAt,
    ownerLastReceivedAt = u.ownerLastReceivedAt,
    ownerLastReadAt = u.ownerLastReadAt,
    shownAt = u.shownAt,
    isFavourite = u.isFavourite,
    archivedAt = u.archivedAt,
    createdAt = u.createdAt
  )

}

case class DialogObsolete(
  userId:              Int,
  peer:                Peer,
  lastMessageDate:     DateTime,
  lastReceivedAt:      DateTime,
  lastReadAt:          DateTime,
  ownerLastReceivedAt: DateTime,
  ownerLastReadAt:     DateTime,
  shownAt:             Option[DateTime],
  isFavourite:         Boolean,
  archivedAt:          Option[DateTime],
  createdAt:           DateTime
)

case class DialogCommon(
  dialogId:        String,
  lastMessageDate: DateTime,
  lastReceivedAt:  DateTime,
  lastReadAt:      DateTime
)

case class UserDialog(
  userId:              Int,
  peer:                Peer,
  ownerLastReceivedAt: DateTime,
  ownerLastReadAt:     DateTime,
  createdAt:           DateTime,
  shownAt:             Option[DateTime],
  isFavourite:         Boolean,
  archivedAt:          Option[DateTime]
)