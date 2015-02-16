package im.actor.server.api.mtproto.protocol

@SerialVersionUID(1L)
case class RequestAuthId() extends ProtoMessage {
  val header = RequestAuthId.header
}

object RequestAuthId {
  val header = 0xF0
}
