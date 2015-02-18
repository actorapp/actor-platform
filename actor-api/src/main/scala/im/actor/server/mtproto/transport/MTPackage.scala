package im.actor.server.mtproto.transport

import scodec.bits.BitVector

@SerialVersionUID(1L)
case class MTPackage(authId: Long, sessionId: Long, messageBytes: BitVector) extends MTProto

object MTPackage {
  val header = 0x0
}
