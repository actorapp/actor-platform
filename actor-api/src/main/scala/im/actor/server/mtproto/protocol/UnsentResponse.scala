package im.actor.server.mtproto.protocol

@SerialVersionUID(1L)
case class UnsentResponse(messageId: Long, requestMessageId: Long, length: Int) extends ProtoMessage {
  val header = UnsentResponse.header
}

object UnsentResponse {
  val header = 0x08
}
