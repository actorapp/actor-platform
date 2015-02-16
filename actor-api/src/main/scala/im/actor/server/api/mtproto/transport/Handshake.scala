package im.actor.server.api.mtproto.transport

import scodec.bits.BitVector

@SerialVersionUID(1L)
case class Handshake(protoVersion: Byte, apiMajorVersion: Byte, apiMinorVersion: Byte,
                     randomBytes: BitVector) extends MTTransport
