package im.actor.server.mtproto.transport

@SerialVersionUID(1L)
case class InternalError(errorCode: Byte, retryTimeout: Int, msg: String) extends MTProto {
  val header = InternalError.header
}

object InternalError {
  val header = 0x5
}
