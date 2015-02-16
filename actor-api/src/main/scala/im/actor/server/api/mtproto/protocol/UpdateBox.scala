package im.actor.server.api.mtproto.protocol

import scodec.bits.BitVector

@SerialVersionUID(1L)
case class UpdateBox(bodyBytes: BitVector) extends ProtoMessage {
  val header = UpdateBox.header
}

object UpdateBox {
  val header = 0x05
}
