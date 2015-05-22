package im.actor.server.mtproto.transport

// FIXME: rename to FRAME
@SerialVersionUID(1L)
case class TransportPackage(index: Int, body: MTProto)
