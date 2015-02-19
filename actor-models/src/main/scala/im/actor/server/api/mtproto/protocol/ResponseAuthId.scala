package im.actor.server.mtproto.protocol

@SerialVersionUID(1L)
case class ResponseAuthId(authId: Long) extends ProtoMessage {
  val header = ResponseAuthId.header
}

object ResponseAuthId {
  val header = 0xF1
}
