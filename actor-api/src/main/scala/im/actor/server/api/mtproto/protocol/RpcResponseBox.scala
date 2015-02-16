package im.actor.server.api.mtproto.protocol

import scodec.bits.BitVector

@SerialVersionUID(1L)
case class RpcResponseBox(messageId: Long, bodyBytes: BitVector) extends ProtoMessage {
  val header = RpcResponseBox.header
}

object RpcResponseBox {
  val header = 0x04
}
