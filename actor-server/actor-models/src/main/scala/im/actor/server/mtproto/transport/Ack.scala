package im.actor.server.mtproto.transport

@SerialVersionUID(1L)
case class Ack(receiedPackageIndex: Int) extends MTProto {
  val header = Ack.header
}

object Ack {
  val header = 0x6
}