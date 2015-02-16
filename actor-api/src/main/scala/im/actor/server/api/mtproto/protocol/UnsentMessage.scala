package im.actor.server.api.mtproto.protocol

@SerialVersionUID(1L)
case class UnsentMessage(messageId: Long, length: Int) extends ProtoMessage {
  val header = UnsentMessage.header
}

object UnsentMessage {
  val header = 0x07
}
