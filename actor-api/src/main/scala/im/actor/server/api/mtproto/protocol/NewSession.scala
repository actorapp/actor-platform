package im.actor.server.api.mtproto.protocol

@SerialVersionUID(1L)
case class NewSession(sessionId: Long, messageId: Long) extends ProtoMessage {
  val header = NewSession.header
}

object NewSession {
  val header = 0x0C
}
