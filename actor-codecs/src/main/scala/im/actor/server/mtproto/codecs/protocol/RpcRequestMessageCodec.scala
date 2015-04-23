package im.actor.server.mtproto.codecs.protocol

import im.actor.server.mtproto.codecs._
import im.actor.server.mtproto.protocol._
import scodec.bits.BitVector
import scodec._
import scodec.codecs._

object RpcRequestMessageCodec extends Codec[RpcRequestMessage] {
  def sizeBound = SizeBound.unknown

  private val rpcRequestCodec = discriminated[RpcRequestMessage].by(uint8)
    //    .\(RpcRequest.header) { case r: RpcRequest => r} (RpcRequestCodec)
    .\(0, _ ⇒ true) { case a ⇒ a }(DiscriminatedErrorCodec("RpcRequestBox"))

  private val codec = PayloadCodec(rpcRequestCodec)

  def encode(r: RpcRequestMessage) = codec.encode(r)

  def decode(buf: BitVector) = codec.decode(buf)
}
