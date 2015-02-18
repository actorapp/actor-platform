package im.actor.server.mtproto.codecs

import scodec.Codec
import scodec.bits._

object BytesCodec extends Codec[BitVector] {
  import im.actor.server.api.util.ByteConstants._

  def encode(b: BitVector) = {
    for { length <- varint.encode(b.length / byteSize) }
    yield length ++ b
  }

  def decode(buf: BitVector) = {
    for { t <- varint.decode(buf) }
    yield {
      val (b, bitLength) = t
      val length = bitLength * byteSize
      (b.drop(length), b.take(length))
    }
  }
}
