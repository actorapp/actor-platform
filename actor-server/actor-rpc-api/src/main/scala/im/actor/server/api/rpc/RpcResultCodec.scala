package im.actor.server.api.rpc

import scodec._
import scodec.bits.BitVector
import scodec.codecs._

import im.actor.api.rpc.RpcResult

object RpcResultCodec extends Codec[RpcResult] {
  def sizeBound = SizeBound.unknown

  private val codec = discriminated[RpcResult].by(uint8)
    .typecase(1, RpcOkCodec)
    .typecase(2, RpcErrorCodec)
    .typecase(4, RpcInternalErrorCodec)

  def encode(res: RpcResult) = codec.encode(res)

  def decode(buf: BitVector) = codec.decode(buf).map {
    case DecodeResult(rsp, rem) ⇒ DecodeResult(rsp, rem)
  }
}
