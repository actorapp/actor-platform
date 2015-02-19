package im.actor.server.mtproto.transport

@SerialVersionUID(1L)
case class Drop(messageId: Long, errorCode: Byte, message: String) extends MTProto

object Drop {
  val header = 0x3
}
