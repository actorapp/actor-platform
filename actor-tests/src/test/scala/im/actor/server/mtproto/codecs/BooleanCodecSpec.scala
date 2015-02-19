package im.actor.server.mtproto.codecs

import scodec.bits._
import scodec._
import org.scalacheck._
import org.scalacheck.Prop._
import org.specs2.mutable.Specification

object BooleanCodecProp extends Properties("BooleanCodec") {
  property("encode/decode") = forAll { (b: Boolean) =>
    val tail = BitVector(hex"feed")
    val buf = boolean.encode(b).require ++ tail
    boolean.decode(buf).require == DecodeResult(b, tail)
  }
}

class BooleanCodecSpec extends Specification {
  "BooleanCodecSpec" should {
    "encode Boolean" in {
      boolean.encode(true).require should_== hex"1".bits
      boolean.encode(false).require should_== hex"0".bits
    }

    "decode bytes to Boolean" in {
      val tail = BitVector(hex"feed")
      boolean.decode(hex"00feed".bits).require should_== DecodeResult(false, tail)
      boolean.decode(hex"01feed".bits).require should_== DecodeResult(true, tail)
      boolean.decode(hex"fffeed".bits).require should_== DecodeResult(true, tail)
    }
  }
}
