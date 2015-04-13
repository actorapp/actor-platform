package im.actor.server.mtproto.transport

import scodec.bits.BitVector

sealed trait MTTransport

@SerialVersionUID(1L)
case class HandshakeHeader(protoVersion: Byte,
                           apiMajorVersion: Byte,
                           apiMinorVersion: Byte,
                           dataLength: Int)

@SerialVersionUID(1L)
case class Handshake(protoVersion: Byte,
                     apiMajorVersion: Byte,
                     apiMinorVersion: Byte,
                     bytes: BitVector) extends MTTransport

@SerialVersionUID(1L)
case class ProtoPackage(m: MTProto) extends MTTransport

@SerialVersionUID(1L)
case object SilentClose extends MTTransport
