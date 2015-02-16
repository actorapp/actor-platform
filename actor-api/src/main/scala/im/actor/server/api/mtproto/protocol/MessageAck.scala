package im.actor.server.api.mtproto.protocol

@SerialVersionUID(1L)
case class MessageAck(messageIds: Vector[Long]) extends ProtoMessage {
  val header = MessageAck.header
}

object MessageAck {
  val header = 0x06
}
