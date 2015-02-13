package im.actor.server.api.mtproto.codecs

import scodec.Codec
import scodec.bits._

object BytesCodec extends Codec[BitVector] {
  import im.actor.server.api.util.ByteConstants._

  def encode(b: BitVector) = {
    for { length <- varint.encode(b.length / byteSize) }
    yield length ++ b
  }

  def decode(buf: BitVector) = {
    for {
      t <- varint.decode(buf)
      (b, bitLength) = t
    } yield {
      val length = bitLength * byteSize
      (b.drop(length), b.take(length))
    }
  }
}
