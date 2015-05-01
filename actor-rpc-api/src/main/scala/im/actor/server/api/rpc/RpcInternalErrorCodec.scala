package im.actor.server.api.rpc

import scodec._
import scodec.bits.BitVector
import scodec.codecs._

import im.actor.api.rpc.RpcInternalError
import im.actor.server.mtproto.codecs._

object RpcInternalErrorCodec extends Codec[RpcInternalError] {
  def sizeBound = SizeBound.unknown

  private val codec = (BooleanCodec :: int32).as[RpcInternalError]

  def encode(err: RpcInternalError) = codec.encode(err)

  def decode(buf: BitVector) = codec.decode(buf)
}
