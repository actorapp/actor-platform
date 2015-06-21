package im.actor.server.mtproto.codecs

import org.scalacheck.Prop._
import org.scalacheck._
import org.scalatest.{ FlatSpec, Matchers }
import scodec._
import scodec.bits._

object BooleanCodecProp extends Properties("BooleanCodec") {
  property("encode/decode") = forAll { (b: Boolean) ⇒
    val tail = BitVector(hex"feed")
    val buf = boolean.encode(b).require ++ tail
    boolean.decode(buf).require == DecodeResult(b, tail)
  }
}

class BooleanCodecSpec extends FlatSpec with Matchers {
  "BooleanCodecSpec" should "encode Boolean" in {
    boolean.encode(true).require should ===(hex"1".bits)
    boolean.encode(false).require should ===(hex"0".bits)
  }

  it should "decode bytes to Boolean" in {
    val tail = BitVector(hex"feed")
    boolean.decode(hex"00feed".bits).require should ===(DecodeResult(false, tail))
    boolean.decode(hex"01feed".bits).require should ===(DecodeResult(true, tail))
    boolean.decode(hex"fffeed".bits).require should ===(DecodeResult(true, tail))
  }
}
