package im.actor.server.mtproto.codecs

import im.actor.server.mtproto.protocol._
import scodec.codecs._

package object protocol {
  val EncryptedPackageCodec = (int64 :: bytes).as[EncryptedPackage]
  val EncryptionCBCPackageCodec = (bytes :: bytes).as[EncryptionCBCPackage]

  val MessageAckCodec = longs.as[MessageAck]
  val NewSessionCodec = (int64 :: int64).as[NewSession]
  val AuthIdInvalidCodec = provide[AuthIdInvalid](AuthIdInvalid)
  val SessionHelloCodec = provide[SessionHello](SessionHello)
  val SessionLostCodec = provide[SessionLost](SessionLost)
  val RequestAuthIdCodec = provide[RequestAuthId](RequestAuthId)
  val RequestResendCodec = int64.as[RequestResend]
  val ResponseAuthIdCodec = int64.as[ResponseAuthId]
  val RequestStartAuthCodec = int64.as[RequestStartAuth]
  val ResponseStartAuthCodec = (int64 :: longs :: bytes).as[ResponseStartAuth]
  val RequestGetServerKeyCodec = int64.as[RequestGetServerKey]
  val ResponseGetServerKeyCodec = (int64 :: bytes).as[ResponseGetServerKey]
  val RequestDHCodec = (int64 :: int64 :: bytes :: bytes).as[RequestDH]
  val ResponseDoDHCodec = (int64 :: bytes :: bytes).as[ResponseDoDH]
  val ProtoRpcRequestCodec = bytes.as[ProtoRpcRequest]
  val ProtoRpcResponseCodec = (int64 :: bytes).as[ProtoRpcResponse]
  val UnsentMessageCodec = (int64 :: int32).as[UnsentMessage]
  val UnsentResponseCodec = (int64 :: int64 :: int32).as[UnsentResponse]
  val ProtoPushCodec = bytes.as[ProtoPush]
}
