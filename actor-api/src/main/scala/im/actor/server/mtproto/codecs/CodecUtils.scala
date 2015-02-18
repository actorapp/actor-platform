package im.actor.server.mtproto.codecs

import scodec.bits.BitVector
import java.util.zip.CRC32

object CodecUtils {
  def crc32(buf: BitVector): BitVector = {
    val ins = new CRC32()
    ins.update(buf.toByteBuffer)
    BitVector(ins.getValue)
  }
}
