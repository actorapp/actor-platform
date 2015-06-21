package im.actor.server.mtproto.codecs.transport

import scodec._
import scodec.bits._
import scodec.{ codecs ⇒ C }

import im.actor.server.mtproto.codecs._

object IntLengthBitsCodec extends Codec[BitVector] {
  override def sizeBound = SizeBound.unknown

  override def encode(b: BitVector) = {
    for {
      // FIXME: check if fits into int32
      length ← C.int32.encode((b.length / byteSize).toInt)
    } yield (length ++ b)
  }

  override def decode(b: BitVector) = {
    for {
      lengthRes ← C.int32.decode(b)
      bytesRes ← C.bits(lengthRes.value * byteSize).decode(lengthRes.remainder)
    } yield DecodeResult(bytesRes.value, bytesRes.remainder)
  }
}
