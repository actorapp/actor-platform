package im.actor.server.api.mtproto.codecs

import scodec.Codec
import scodec.bits._

object LongsCodec extends Codec[Vector[Long]] {
  import im.actor.server.api.util.ByteConstants._

  def encode(v: Vector[Long]) = {
    for { length <- varint.encode(v.size) }
    yield v.map(BitVector.fromLong(_)).foldLeft(length)(_ ++ _)
  }

  def decode(buf: BitVector) = {
    for {
      t <- varint.decode(buf)
      (b, bitLength) = t
    } yield {
      val length = bitLength * longBits
      (b.drop(length), b.take(length).grouped(longBits).map(_.toLong()).toVector)
    }
  }
}
