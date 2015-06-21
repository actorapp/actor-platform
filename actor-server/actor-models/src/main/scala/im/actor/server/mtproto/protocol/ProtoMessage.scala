package im.actor.server.mtproto.protocol

import scodec.bits.BitVector

sealed trait ProtoMessageWithHeader {
  val header: Int
}

sealed trait ProtoMessage extends ProtoMessageWithHeader

trait RpcRequestMessage extends ProtoMessageWithHeader
trait RpcResponseMessage extends ProtoMessageWithHeader
trait UpdateMessage extends ProtoMessageWithHeader

sealed trait IncomingProtoMessage

sealed trait OutgoingProtoMessage

sealed trait ResendableProtoMessage {
  def bodySize: Int
}

@SerialVersionUID(1L)
case class MessageAck(messageIds: Vector[Long]) extends ProtoMessage {
  val header = MessageAck.header
}

object MessageAck {
  val header = 0x06

  def incoming(messageIds: Seq[Long]): MessageAck with IncomingProtoMessage =
    new MessageAck(messageIds.toVector) with IncomingProtoMessage

  def outgoing(messageIds: Seq[Long]): MessageAck with OutgoingProtoMessage =
    new MessageAck(messageIds.toVector) with OutgoingProtoMessage
}

@SerialVersionUID(1L)
case class Container(messages: Seq[MessageBox]) extends ProtoMessage {
  val header = Container.header
}

object Container {
  val header = 0x0A
}

trait AuthIdInvalid extends ProtoMessage with OutgoingProtoMessage {
  val header = 0x11
}

@SerialVersionUID(1L)
case object AuthIdInvalid extends AuthIdInvalid

@SerialVersionUID(1L)
case class NewSession(sessionId: Long, messageId: Long) extends ProtoMessage with OutgoingProtoMessage with ResendableProtoMessage {
  val header = NewSession.header

  override def bodySize = 0
}

object NewSession {
  val header = 0x0C
}

trait SessionHello extends ProtoMessage with IncomingProtoMessage {
  val header = 0x0F
}

@SerialVersionUID(1L)
case object SessionHello extends SessionHello

trait SessionLost extends ProtoMessage with OutgoingProtoMessage {
  val header = 0x10
}

@SerialVersionUID(1L)
case object SessionLost extends SessionLost

@SerialVersionUID(1L)
trait RequestAuthId extends ProtoMessage with OutgoingProtoMessage {
  val header = 0xF0
}

object RequestAuthId extends RequestAuthId

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
case class RpcResponseBox(messageId: Long, bodyBytes: BitVector) extends ProtoMessage with OutgoingProtoMessage with ResendableProtoMessage {
  val header = RpcResponseBox.header

  override val bodySize = bodyBytes.bytes.size
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

@SerialVersionUID(1L)
case class UpdateBox(bodyBytes: BitVector) extends ProtoMessage with OutgoingProtoMessage with ResendableProtoMessage {
  val header = UpdateBox.header

  override val bodySize = bodyBytes.bytes.size
}

object UpdateBox {
  val header = 0x05
}
