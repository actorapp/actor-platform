package im.actor.server.api.mtproto.codecs

import scodec.bits._
import org.scalacheck._
import org.scalacheck.Prop._
import scalaz._
import Scalaz._
import org.specs2.mutable.Specification

object BooleanCodecProp extends Properties("BooleanCodec") {
  property("encode/decode") = forAll { (b: Boolean) =>
    val tail = BitVector(hex"feed")
    val buf = boolean.encode(b).toOption.get ++ tail
    boolean.decode(buf) == (tail, b).right
  }
}

class BooleanCodecSpec extends Specification {
  "BooleanCodecSpec" should {
    "encode Boolean" in {
      boolean.encode(true) should_== hex"1".bits.right
      boolean.encode(false) should_== hex"0".bits.right
    }

    "decode bytes to Boolean" in {
      val tail = BitVector(hex"feed")
      boolean.decode(hex"00feed".bits) should_== (tail, false).right
      boolean.decode(hex"01feed".bits) should_== (tail, true).right
      boolean.decode(hex"fffeed".bits) should_== (tail, true).right
    }
  }
}
