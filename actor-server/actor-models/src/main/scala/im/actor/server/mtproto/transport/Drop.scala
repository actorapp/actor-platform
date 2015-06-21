package im.actor.server.mtproto.transport

@SerialVersionUID(1L)
case class Drop(messageId: Long, errorCode: Byte, message: String) extends MTProto {
  val header = Drop.header
}

object Drop {
  val header = 0x3
}
