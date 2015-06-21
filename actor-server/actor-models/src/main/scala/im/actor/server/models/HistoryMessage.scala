package im.actor.server.models

import org.joda.time.DateTime

case class HistoryMessage(
  userId:               Int,
  peer:                 Peer,
  date:                 DateTime,
  senderUserId:         Int,
  randomId:             Long,
  messageContentHeader: Int,
  messageContentData:   Array[Byte],
  deletedAt:            Option[DateTime]
)
