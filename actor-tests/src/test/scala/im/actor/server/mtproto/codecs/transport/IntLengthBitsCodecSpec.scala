package im.actor.server.mtproto.codecs.transport

import org.specs2.Specification
import org.specs2.matcher.ThrownExpectations
import scodec.bits.BitVector

class IntLengthBitsCodecSpec extends Specification with ThrownExpectations {
  def is = s2"""
             IntLengthBitsCodec should encode/decode $e2
             """

  def e2 = {
    val codec = IntLengthBitsCodec

    val bits = BitVector(1, 2, 3, 4, 5, 6, 7, 8)
    val encoded = codec.encode(bits).require

    codec.decode(encoded).require.value should_== (bits)
  }
}