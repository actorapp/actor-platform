package im.actor.server.mtproto.codecs

import scodec.{ Codec, Err }
import scodec.bits.BitVector
import scalaz._
import Scalaz._

class DiscriminatedErrorCodec[T](codecName: String) extends Codec[T] {
  def encode(a: T) = Err(s"$codecName.header is unknown for ${a.getClass.getCanonicalName}").left

  def decode(buf: BitVector) = Err(s"$codecName.header is unknown. Body: ${buf.toHex}").left
}

object DiscriminatedErrorCodec {
  def apply[T](codecName: String) = new DiscriminatedErrorCodec[T](codecName)
}
