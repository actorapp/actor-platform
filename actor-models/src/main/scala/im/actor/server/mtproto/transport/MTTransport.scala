package im.actor.server.mtproto.transport

import scodec.bits.BitVector

sealed trait MTTransport

@SerialVersionUID(1L)
case class ProtoPackage(m: MTProto) extends MTTransport

@SerialVersionUID(1L)
case object SilentClose extends MTTransport
