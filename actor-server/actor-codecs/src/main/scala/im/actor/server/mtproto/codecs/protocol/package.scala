package im.actor.server.mtproto.codecs

import im.actor.server.mtproto.protocol._
import scodec.codecs._

package object protocol {
  val MessageAckCodec = longs.as[MessageAck]
  val NewSessionCodec = (int64 :: int64).as[NewSession]
  val AuthIdInvalidCodec = provide[AuthIdInvalid](AuthIdInvalid)
  val SessionHelloCodec = provide[SessionHello](SessionHello)
  val SessionLostCodec = provide[SessionLost](SessionLost)
  val RequestAuthIdCodec = provide[RequestAuthId](RequestAuthId)
  val RequestResendCodec = int64.as[RequestResend]
  val ResponseAuthIdCodec = int64.as[ResponseAuthId]
  val RpcRequestBoxCodec = bytes.as[RpcRequestBox]
  val RpcResponseBoxCodec = (int64 :: bytes).as[RpcResponseBox]
  val UnsentMessageCodec = (int64 :: int32).as[UnsentMessage]
  val UnsentResponseCodec = (int64 :: int64 :: int32).as[UnsentResponse]
  val UpdateBoxCodec = bytes.as[UpdateBox]
}
