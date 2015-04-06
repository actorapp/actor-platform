package im.actor.server.mtproto.protocol

import scodec.bits.BitVector

sealed trait ProtoMessageWithHeader {
  val header: Int
}

sealed trait ProtoMessage extends ProtoMessageWithHeader

trait RpcRequestMessage extends ProtoMessageWithHeader
trait RpcResponseMessage extends ProtoMessageWithHeader
trait UpdateMessage extends ProtoMessageWithHeader

@SerialVersionUID(1L)
case class MessageAck(messageIds: Vector[Long]) extends ProtoMessage {
  val header = MessageAck.header
}

object MessageAck {
  val header = 0x06
}

@SerialVersionUID(1L)
case class Container(messages: Seq[MessageBox]) extends ProtoMessage {
  val header = Container.header
}

object Container {
  val header = 0x0A
}

@SerialVersionUID(1L)
case class NewSession(sessionId: Long, messageId: Long) extends ProtoMessage {
  val header = NewSession.header
}

object NewSession {
  val header = 0x0C
}

@SerialVersionUID(1L)
case class RequestAuthId() extends ProtoMessage {
  val header = RequestAuthId.header
}

object RequestAuthId {
  val header = 0xF0
}

@SerialVersionUID(1L)
case class RequestResend(messageId: Long) extends ProtoMessage {
  val header = RequestResend.header
}

object RequestResend {
  val header = 0x09
}

@SerialVersionUID(1L)
case class ResponseAuthId(authId: Long) extends ProtoMessage {
  val header = ResponseAuthId.header
}

object ResponseAuthId {
  val header = 0xF1
}

@SerialVersionUID(1L)
case class RpcRequestBox(bodyBytes: BitVector) extends ProtoMessage {
  val header = RpcRequestBox.header
}

object RpcRequestBox {
  val header = 0x03
}

@SerialVersionUID(1L)
case class RpcResponseBox(messageId: Long, bodyBytes: BitVector) extends ProtoMessage {
  val header = RpcResponseBox.header
}

object RpcResponseBox {
  val header = 0x04
}

@SerialVersionUID(1L)
case class UnsentMessage(messageId: Long, length: Int) extends ProtoMessage {
  val header = UnsentMessage.header
}

object UnsentMessage {
  val header = 0x07
}

@SerialVersionUID(1L)
case class UnsentResponse(messageId: Long, requestMessageId: Long, length: Int) extends ProtoMessage {
  val header = UnsentResponse.header
}

object UnsentResponse {
  val header = 0x08
}

import scodec.bits.BitVector

@SerialVersionUID(1L)
case class UpdateBox(bodyBytes: BitVector) extends ProtoMessage {
  val header = UpdateBox.header
}

object UpdateBox {
  val header = 0x05
}
