package im.actor.server.mtproto.codecs

import scodec._
import scodec.bits.BitVector

class PayloadCodec[A](codec: Codec[A]) extends Codec[A] {
  def sizeBound = SizeBound.unknown

  def encode(v: A) = codec.encode(v).flatMap(bytes.encode)

  def decode(buf: BitVector) = {
    for {
      t ← bytes.decode(buf)
      res ← codec.decode(t.value)
    } yield DecodeResult(res.value, t.remainder)
  }
}

object PayloadCodec {
  def apply[A](codec: Codec[A]): Codec[A] = new PayloadCodec(codec)
}
