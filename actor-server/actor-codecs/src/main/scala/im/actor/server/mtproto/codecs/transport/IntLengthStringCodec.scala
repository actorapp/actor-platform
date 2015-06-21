package im.actor.server.mtproto.codecs.transport

import scodec._
import scodec.bits._
import scodec.{ codecs ⇒ C }

import im.actor.server.mtproto.codecs._

object IntLengthStringCodec extends Codec[String] {
  override def sizeBound = SizeBound.unknown

  override def encode(str: String) = {
    val strBytes = str.getBytes

    for {
      // FIXME: check if fits into int32
      length ← C.int32.encode(strBytes.length)
    } yield (length ++ BitVector(strBytes))
  }

  override def decode(b: BitVector) = {
    for {
      lengthRes ← C.int32.decode(b)
      strRes ← C.bits(lengthRes.value * byteSize).decode(lengthRes.remainder)
    } yield DecodeResult(new String(strRes.value.toByteArray, "UTF-8"), strRes.remainder)
  }
}
