package im.actor.server.models

import org.joda.time.DateTime
import scodec.bits.BitVector

@SerialVersionUID(1L)
case class Dialog(
  userId:          Int,
  peer:            Peer,
  lastMessageDate: DateTime,
  lastReceivedAt:  DateTime,
  lastReadAt:      DateTime
)
