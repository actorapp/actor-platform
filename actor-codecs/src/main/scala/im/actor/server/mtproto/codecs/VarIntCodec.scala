package im.actor.server.mtproto.codecs

import scala.annotation.tailrec
import scodec._
import scodec.bits._

object VarIntCodec extends Codec[Long] {
  def sizeBound = SizeBound.unknown

  def encode(n: Long) = {
    @inline @tailrec
    def f(bn: Long, buf: BitVector): Attempt[BitVector] = {
      if (bn > 0x7F) f(bn >> 7, buf.++(BitVector((bn & 0xFF) | 0x80)))
      else Attempt.successful(buf.++(BitVector(bn)))
    }
    f(n.abs, BitVector.empty)
  }

  def decode(buf: BitVector) = {
    @inline @tailrec
    def f(index: Long, res: Long): Attempt[DecodeResult[Long]] = {
      if (index > 10L) Attempt.failure(Err("exceeded long size"))
      else {
        val n = res | ((buf.getByte(index) & 0xFFL) & 0x7FL) << index * 7
        if ((buf.length > byteSize * index) && (buf.getByte(index) & 0x80) != 0) f(index + 1, n)
        else Attempt.successful(DecodeResult(n, buf.drop(byteSize * (index + 1))))
      }
    }
    if (buf.isEmpty) Attempt.failure(Err("empty buf"))
    else f(0L, 0L)
  }
}
