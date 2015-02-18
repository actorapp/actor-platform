package im.actor.server.mtproto.codecs

import scodec.{ Codec, Err }
import scodec.bits._
import scalaz._
import Scalaz._

object BooleanCodec extends Codec[Boolean] {
  import im.actor.server.api.util.ByteConstants._

  def encode(b: Boolean) = {
    if (b) BitVector(1).right
    else BitVector(0).right
  }

  def decode(buf: BitVector) = {
    if (buf.isEmpty) Err("empty buf").left
    else (buf.drop(byteSize), buf.getByte(0) != 0).right
  }
}
