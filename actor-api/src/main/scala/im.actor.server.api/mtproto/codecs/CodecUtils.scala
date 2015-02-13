package im.actor.server.api.mtproto.codecs

import akka.util.ByteString
import scodec.bits.BitVector
import java.util.zip.CRC32

object CodecUtils {
  import im.actor.server.api.util.ByteConstants._

  def readVarInt(bs: ByteString): Option[Long] = {
    @inline
    def f(index: Int, res: Long): Long = {
      if (bs.length > index && (bs(index) & 0x80) != 0)
        f(index + 1, res | (bs(index) & 0x7F << index * 7))
      else res
    }
    if (bs.isEmpty) None
    else Some(f(0, 0L))
  }

  def readInt32(bs: ByteString): Option[Int] = {
    if (bs.length != int32Bytes) None
    else Some(bs(0) << 24 + bs(1) << 16 + bs(2) << 8 + bs(3))
  }

  def readBytes(bs: ByteString): Option[ByteString] = {
    val lenBS = bs.takeWhile { c => (c & 0x80) != 0 }
    readVarInt(lenBS).map { l => bs.drop(lenBS.length).take(l.toInt) }
  }

  def crc32(buf: BitVector): BitVector = {
    val ins = new CRC32()
    ins.update(buf.toByteBuffer)
    BitVector(ins.getValue)
  }
}
