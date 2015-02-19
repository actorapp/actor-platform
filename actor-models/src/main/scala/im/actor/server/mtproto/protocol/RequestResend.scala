package im.actor.server.mtproto.protocol

@SerialVersionUID(1L)
case class RequestResend(messageId: Long) extends ProtoMessage {
  val header = RequestResend.header
}

object RequestResend {
  val header = 0x09
}
