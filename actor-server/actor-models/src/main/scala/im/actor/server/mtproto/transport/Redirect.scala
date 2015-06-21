package im.actor.server.mtproto.transport

@SerialVersionUID(1L)
case class Redirect(hostname: String, port: Int, timeout: Int) extends MTProto {
  val header = Redirect.header
}

object Redirect {
  val header = 0x4
}
