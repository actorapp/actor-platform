package im.actor.server.mtproto.transport

import scodec.bits.BitVector

@SerialVersionUID(1L)
case class Ping(randomBytes: BitVector) extends MTProto

object Ping {
  val header = 0x1
}
