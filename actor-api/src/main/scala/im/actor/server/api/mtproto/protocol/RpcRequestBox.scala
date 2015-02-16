package im.actor.server.api.mtproto.protocol

import scodec.bits.BitVector

@SerialVersionUID(1L)
case class RpcRequestBox(bodyBytes: BitVector) extends ProtoMessage {
  val header = RpcRequestBox.header
}

object RpcRequestBox {
  val header = 0x03
}
