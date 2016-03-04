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
final case class NewSession(sessionId: Long, messageId: Long) extends ProtoMessage with OutgoingProtoMessage with ResendableProtoMessage {
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
trait RequestAuthId extends ProtoMessage with IncomingProtoMessage {
  val header = 0xF0
}

object RequestAuthId extends RequestAuthId

@SerialVersionUID(1L)
final case class RequestStartAuth(randomId: Long) extends ProtoMessage with IncomingProtoMessage {
  val header = RequestStartAuth.header
}

object RequestStartAuth {
  val header = 0xE0
}

@SerialVersionUID(1L)
final case class ResponseStartAuth(randomId: Long, availableKeys: Vector[Long], serverNonce: BitVector) extends ProtoMessage with OutgoingProtoMessage {
  val header = ResponseStartAuth.header
}

object ResponseStartAuth {
  val header = 0xE1
}

@SerialVersionUID(1L)
final case class RequestGetServerKey(keyId: Long) extends ProtoMessage with IncomingProtoMessage {
  val header = RequestGetServerKey.header
}

object RequestGetServerKey {
  val header = 0xE2
}

@SerialVersionUID(1L)
final case class ResponseGetServerKey(keyId: Long, key: BitVector) extends ProtoMessage with OutgoingProtoMessage {
  val header = ResponseGetServerKey.header
}

object ResponseGetServerKey {
  val header = 0xE3
}

@SerialVersionUID(1L)
final case class RequestDH(randomId: Long, keyId: Long, clientNonce: BitVector, clientKey: BitVector) extends ProtoMessage with IncomingProtoMessage {
  val header = RequestDH.header
}

object RequestDH {
  val header = 0xE6
}

@SerialVersionUID(1L)
final case class ResponseDoDH(randomId: Long, verify: BitVector, verifySign: BitVector) extends ProtoMessage with OutgoingProtoMessage {
  val header = ResponseDoDH.header
}

object ResponseDoDH {
  val header = 0xE7
}

@SerialVersionUID(1L)
final case class RequestResend(messageId: Long) extends ProtoMessage {
  val header = RequestResend.header
}

object RequestResend {
  val header = 0x09
}

@SerialVersionUID(1L)
final case class ResponseAuthId(authId: Long) extends ProtoMessage {
  val header = ResponseAuthId.header
}

object ResponseAuthId {
  val header = 0xF1
}

@SerialVersionUID(1L)
final case class ProtoRpcRequest(bodyBytes: BitVector) extends ProtoMessage {
  val header = ProtoRpcRequest.header
}

object ProtoRpcRequest {
  val header = 0x03
}

@SerialVersionUID(1L)
final case class ProtoRpcResponse(messageId: Long, bodyBytes: BitVector) extends ProtoMessage with OutgoingProtoMessage with ResendableProtoMessage {
  val header = ProtoRpcResponse.header

  override val bodySize = bodyBytes.bytes.size
}

object ProtoRpcResponse {
  val header = 0x04
}

@SerialVersionUID(1L)
final case class UnsentMessage(messageId: Long, length: Int) extends ProtoMessage {
  val header = UnsentMessage.header
}

object UnsentMessage {
  val header = 0x07
}

@SerialVersionUID(1L)
final case class UnsentResponse(messageId: Long, requestMessageId: Long, length: Int) extends ProtoMessage {
  val header = UnsentResponse.header
}

object UnsentResponse {
  val header = 0x08
}

@SerialVersionUID(1L)
final case class ProtoPush(bodyBytes: BitVector) extends ProtoMessage with OutgoingProtoMessage with ResendableProtoMessage {
  val header = ProtoPush.header

  override val bodySize = bodyBytes.bytes.size
}

object ProtoPush {
  val header = 0x05
}
