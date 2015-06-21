package im.actor.server.mtproto.codecs.transport

import org.scalatest.{ FlatSpec, Matchers }
import scodec.bits.BitVector

class IntLengthBitsCodecSpec extends FlatSpec with Matchers {
  it should "encode/decode" in e2

  def e2() = {
    val codec = IntLengthBitsCodec

    val bits = BitVector(1, 2, 3, 4, 5, 6, 7, 8)
    val encoded = codec.encode(bits).require

    codec.decode(encoded).require.value should ===(bits)
  }
}