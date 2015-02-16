package im.actor.server.api.mtproto.codecs

import im.actor.server.api.mtproto.protocol._
import scodec.codecs._

package object protocol {
  val MessageAckCodec = longs.pxmap[MessageAck](MessageAck.apply, MessageAck.unapply)

  val NewSessionCodec = (int64 :: int64).as[NewSession]

  val RequestAuthIdCodec = provide(RequestAuthId())

  val RequestResendCodec = int64.pxmap[RequestResend](RequestResend.apply, RequestResend.unapply)

  val ResponseAuthIdCodec = int64.pxmap[ResponseAuthId](ResponseAuthId.apply, ResponseAuthId.unapply)

  val RpcRequestBoxCodec = bytes.pxmap[RpcRequestBox](RpcRequestBox.apply, RpcRequestBox.unapply)

  val RpcResponseBoxCodec = (int64 :: bytes).as[RpcResponseBox]

  val UnsentMessageCodec = (int64 :: int32).as[UnsentMessage]

  val UnsentResponseCodec = (int64 :: int64 :: int32).as[UnsentResponse]

  val UpdateBoxCodec = bytes.pxmap[UpdateBox](UpdateBox.apply, UpdateBox.unapply)
}
