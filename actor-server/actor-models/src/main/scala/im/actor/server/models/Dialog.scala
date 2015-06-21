package im.actor.server.models

import org.joda.time.DateTime

@SerialVersionUID(1L)
case class Dialog(
  userId:              Int,
  peer:                Peer,
  lastMessageDate:     DateTime,
  lastReceivedAt:      DateTime,
  lastReadAt:          DateTime,
  ownerLastReceivedAt: DateTime,
  ownerLastReadAt:     DateTime
)
