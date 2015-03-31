package im.actor.server.mtproto.transport

import scodec.bits.BitVector

@SerialVersionUID(1L)
case class Handshake(protoVersion: Byte, apiMajorVersion: Byte, apiMinorVersion: Byte,
                     bytes: BitVector) extends MTTransport
