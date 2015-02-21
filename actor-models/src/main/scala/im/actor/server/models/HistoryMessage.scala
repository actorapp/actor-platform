package im.actor.server.models

import org.joda.time.DateTime
import scodec.bits.BitVector

case class HistoryMessage(
  userId: Int,
  peer: Peer,
  date: DateTime,
  randomId: Long,
  senderUserId: Int,
  messageContentHeader: Int,
  messageContentData: BitVector,
  state: MessageState
)
