package im.actor.server.mtproto.codecs

import org.scalacheck.Prop._
import org.scalacheck._
import org.scalatest.{ FlatSpec, Matchers }
import scodec._
import scodec.bits._
import test.utils.scalacheck.Generators._

object BytesCodecProp extends Properties("BytesCodec") {
  property("encode/decode") = forAll(genBV()) { (a: BitVector) ⇒
    val tail = BitVector(hex"feed")
    val buf = bytes.encode(a).require ++ tail
    bytes.decode(buf).require == DecodeResult(a, tail)
  }
}

class BytesCodecSpec extends FlatSpec with Matchers {
  "BytesCodec" should "encode ByteVector" in {
    val b = hex"feedfeedfeed".bits
    bytes.encode(b).require should ===(hex"6".bits ++ b)
  }

  it should "decode bytes to ByteVector" in {
    val tail = BitVector(hex"feed")
    val b = hex"feedfeedfeed".bits
    val res = bytes.decode(hex"6".bits ++ b ++ tail).require
    res should ===(DecodeResult(b, tail))
  }
}
