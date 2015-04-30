package im.actor.server.mtproto.transport

import scodec.bits.BitVector

@SerialVersionUID(1L)
case class HandshakeHeader(
  protoVersion:    Byte,
  apiMajorVersion: Byte,
  apiMinorVersion: Byte,
  dataLength:      Int
)

@SerialVersionUID(1L)
case class Handshake(
  protoVersion:    Byte,
  apiMajorVersion: Byte,
  apiMinorVersion: Byte,
  bytes:           BitVector
) extends MTProto {
  override val header = Handshake.header
}

object Handshake {
  val header = 0xFF
}