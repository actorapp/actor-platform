package im.actor.util.misc

import java.nio.ByteBuffer

object ByteUtils {
  private val buffer = ByteBuffer.allocate(java.lang.Long.BYTES);

  def longToBytes(x: Long) = {
    buffer.putLong(0, x)
    buffer.array()
  }

  def bytesToLong(bytes: Array[Byte]) = {
    buffer.put(bytes, 0, bytes.length)
    buffer.flip()
    buffer.getLong()
  }
}
