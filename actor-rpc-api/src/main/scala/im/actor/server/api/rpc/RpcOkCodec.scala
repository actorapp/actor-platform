package im.actor.server.api.rpc

import scodec._
import scodec.bits.BitVector

import im.actor.api.rpc.RpcOk
import im.actor.api.rpc.codecs.RpcResponseCodec

object RpcOkCodec extends Codec[RpcOk] {
  def sizeBound = SizeBound.unknown

  private val codec = RpcResponseCodec

  def encode(rok: RpcOk) = codec.encode(rok.response)

  def decode(buf: BitVector) = codec.decode(buf).map {
    case DecodeResult(rsp, rem) ⇒ DecodeResult(RpcOk(rsp), rem)
  }
}
