package im.actor.server.model

import org.joda.time.DateTime

case class HistoryMessage(
  userId:               Int,
  peer:                 Peer,
  date:                 DateTime,
  senderUserId:         Int,
  randomId:             Long,
  messageContentHeader: Int,
  messageContentData:   Array[Byte],
  deletedAt:            Option[DateTime],
  quotedMessagePeer:    Option[Peer]     = None,
  quotedMessageRid:     Option[Long]     = None
) {
  def ofUser(userId: Int) = this.copy(userId = userId)
}
