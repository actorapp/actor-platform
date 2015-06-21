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

@SerialVersionUID(1L)
case class HandshakeResponse(
  protoVersion:    Byte,
  apiMajorVersion: Byte,
  apiMinorVersion: Byte,
  sha256:          BitVector
) extends MTProto {
  override val header = HandshakeResponse.header
}

object HandshakeResponse {
  val header = 0xFE
}