package im.actor.server.mtproto.protocol

@SerialVersionUID(1L)
case class Container(messages: Seq[MessageBox]) extends ProtoMessage {
  val header = Container.header
}

object Container {
  val header = 0x0A
}
