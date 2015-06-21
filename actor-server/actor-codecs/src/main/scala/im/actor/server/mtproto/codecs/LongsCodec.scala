package im.actor.server.mtproto.codecs

import scodec._
import scodec.bits._

object LongsCodec extends Codec[Vector[Long]] {
  def sizeBound = SizeBound.unknown

  def encode(v: Vector[Long]) = {
    for { length ← varint.encode(v.size.toLong) }
      yield v.map(BitVector.fromLong(_)).foldLeft(length)(_ ++ _)
  }

  def decode(buf: BitVector) = {
    for { t ← varint.decode(buf) } yield {
      val length = t.value * longBits
      DecodeResult(t.remainder.take(length).grouped(longBits).map(_.toLong()).toVector, t.remainder.drop(length))
    }
  }
}
