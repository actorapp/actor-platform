package im.actor.server.api.mtproto.codecs

import scala.annotation.tailrec
import scodec.{ Codec, Err }
import scodec.bits._
import scalaz._
import Scalaz._

object VarIntCodec extends Codec[Long] {
  import im.actor.server.api.util.ByteConstants._

  def encode(n: Long) = {
    @inline @tailrec
    def f(bn: Long, buf: BitVector): Err \/ BitVector = {
      if (bn > 0x7F) f(bn >> 7, buf.++(BitVector((bn & 0xFF) | 0x80)))
      else buf.++(BitVector(bn)).right
    }
    f(n.abs, BitVector.empty)
  }

  def decode(buf: BitVector) = {
    @inline @tailrec
    def f(index: Int, res: Long): Err \/ (BitVector, Long) = {
      if (index > 10) Err("exceeded long size").left
      else {
        val n = res | ((buf.getByte(index) & 0xFFL) & 0x7FL) << index * 7
        if ((buf.length > byteSize * index) && (buf.getByte(index) & 0x80) != 0) f(index + 1, n)
        else (buf.drop(byteSize * (index + 1)), n).right
      }
    }
    if (buf.isEmpty) Err("empty buf").left
    else f(0, 0L)
  }
}
