package im.actor.server.mtproto.transport

import scodec.bits.BitVector

@SerialVersionUID(1L)
final case class Package(authId: Long, sessionId: Long, messageBytes: BitVector) extends MTProto {
  val header = Package.header
}

object Package {
  val header = 0x0
}
