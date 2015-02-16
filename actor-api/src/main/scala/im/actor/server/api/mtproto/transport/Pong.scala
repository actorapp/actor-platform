package im.actor.server.api.mtproto.transport

import scodec.bits.BitVector

@SerialVersionUID(1L)
case class Pong(randomBytes: BitVector) extends MTProto

object Pong {
  val header: Byte = 2
}
