package im.actor.server.api.mtproto.transport

@SerialVersionUID(1L)
case class InternalError(errorCode: Byte, retryTimeout: Int, msg: String) extends MTProto

object InternalError {
  val header: Byte = 5
}
