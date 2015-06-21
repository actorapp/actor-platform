package im.actor.server.mtproto.codecs

import scodec._
import scodec.bits._

object BytesCodec extends Codec[BitVector] {
  def sizeBound = SizeBound.unknown

  def encode(b: BitVector) = {
    for { length ← varint.encode(b.length / byteSize) }
      yield length ++ b
  }

  def decode(buf: BitVector) = {
    for { t ← varint.decode(buf) } yield {
      val length = t.value * byteSize
      DecodeResult(t.remainder.take(length), t.remainder.drop(length))
    }
  }
}
