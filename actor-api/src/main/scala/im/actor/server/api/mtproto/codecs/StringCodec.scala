package im.actor.server.api.mtproto.codecs

import scodec.Codec
import scodec.bits._

object StringCodec extends Codec[String] {
  import im.actor.server.api.util.ByteConstants._

  def encode(str: String) = {
    val strBytes = str.getBytes
    for { length <- varint.encode(strBytes.length) }
    yield length ++ BitVector(strBytes)
  }

  def decode(buf: BitVector) = {
    for {
      t <- varint.decode(buf)
      (b, bitLength) = t
    } yield {
      val length = bitLength * byteSize
      (b.drop(length), new String(b.take(length).toByteArray))
    }
  }
}
