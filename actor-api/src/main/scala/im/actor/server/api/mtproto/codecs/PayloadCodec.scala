package im.actor.server.api.mtproto.codecs

import scodec.Codec
import scodec.bits.BitVector

class PayloadCodec[A](codec: Codec[A]) extends Codec[A] {
  def encode(v: A) = codec.encode(v).flatMap(bytes.encode)

  def decode(buf: BitVector) = {
    for {
      bytesTup <- bytes.decode(buf)
      (xs, body) = bytesTup
      res <- codec.decode(body)
    } yield (xs, res._2)
  }
}

object PayloadCodec {
  def apply[A](codec: Codec[A]): Codec[A] = new PayloadCodec(codec)
}
