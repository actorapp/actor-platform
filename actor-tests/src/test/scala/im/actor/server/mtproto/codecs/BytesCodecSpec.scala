package im.actor.server.mtproto.codecs

import test.utils.scalacheck.Generators._
import scodec.bits._
import scodec._
import org.scalacheck._
import org.scalacheck.Prop._
import org.specs2.mutable.Specification

object BytesCodecProp extends Properties("BytesCodec") {
  property("encode/decode") = forAll(genBV()) { (a: BitVector) =>
    val tail = BitVector(hex"feed")
    val buf = bytes.encode(a).require ++ tail
    bytes.decode(buf).require == DecodeResult(a, tail)
  }
}

class BytesCodecSpec extends Specification {
  "BytesCodec" should {
    "encode ByteVector" in {
      val b = hex"feedfeedfeed".bits
      bytes.encode(b).require should_== (hex"6".bits ++ b)
    }

    "decode bytes to ByteVector" in {
      val tail = BitVector(hex"feed")
      val b = hex"feedfeedfeed".bits
      val res = bytes.decode(hex"6".bits ++ b ++ tail).require
      res should_== DecodeResult(b, tail)
    }
  }
}
