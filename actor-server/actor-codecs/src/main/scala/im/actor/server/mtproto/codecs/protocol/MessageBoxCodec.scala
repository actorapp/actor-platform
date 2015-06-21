package im.actor.server.mtproto.codecs.protocol

import im.actor.server.mtproto.codecs._
import im.actor.server.mtproto.protocol._
import scodec.bits.BitVector
import scodec.codecs._
import scodec._

object MessageBoxCodec extends Codec[MessageBox] {
  def sizeBound = SizeBound.unknown

  private val protoMessageCodec = discriminated[ProtoMessage].by(uint8)
    .\(Container.header) { case r: Container ⇒ r }(ContainerCodec)
    .\(MessageAck.header) { case r: MessageAck ⇒ r }(MessageAckCodec)
    .\(AuthIdInvalid.header) { case r: AuthIdInvalid ⇒ r }(AuthIdInvalidCodec)
    .\(NewSession.header) { case r: NewSession ⇒ r }(NewSessionCodec)
    .\(SessionHello.header) { case r: SessionHello ⇒ r }(SessionHelloCodec)
    .\(SessionLost.header) { case r: SessionLost ⇒ r }(SessionLostCodec)
    .\(RequestAuthId.header) { case r: RequestAuthId ⇒ r }(RequestAuthIdCodec)
    .\(ResponseAuthId.header) { case r: ResponseAuthId ⇒ r }(ResponseAuthIdCodec)
    .\(RequestResend.header) { case r: RequestResend ⇒ r }(RequestResendCodec)
    .\(RpcRequestBox.header) { case r: RpcRequestBox ⇒ r }(RpcRequestBoxCodec)
    .\(RpcResponseBox.header) { case r: RpcResponseBox ⇒ r }(RpcResponseBoxCodec)
    .\(UnsentMessage.header) { case r: UnsentMessage ⇒ r }(UnsentMessageCodec)
    .\(UnsentResponse.header) { case r: UnsentResponse ⇒ r }(UnsentResponseCodec)
    .\(UpdateBox.header) { case r: UpdateBox ⇒ r }(UpdateBoxCodec)
    .\(0, _ ⇒ true) { case a ⇒ a }(DiscriminatedErrorCodec("MessageBox"))

  private val codec = (int64 :: PayloadCodec(protoMessageCodec)).as[MessageBox]

  def encode(mb: MessageBox) = codec.encode(mb)

  def decode(buf: BitVector) = codec.decode(buf)
}
