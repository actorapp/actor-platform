package im.actor.server.mtproto.codecs

import test.utils.scalacheck.Generators._
import scodec.bits._
import org.scalacheck._
import org.scalacheck.Prop._
import scalaz._
import Scalaz._
import org.specs2.mutable.Specification

object BytesCodecProp extends Properties("BytesCodec") {
  property("encode/decode") = forAll(genBV()) { (a: BitVector) =>
    val tail = BitVector(hex"feed")
    val buf = bytes.encode(a).toOption.get ++ tail
    bytes.decode(buf) == (tail, a).right
  }
}

class BytesCodecSpec extends Specification {
  "BytesCodec" should {
    "encode ByteVector" in {
      val b = hex"feedfeedfeed".bits
      bytes.encode(b) should_== (hex"6".bits ++ b).right
    }

    "decode bytes to ByteVector" in {
      val tail = BitVector(hex"feed")
      val b = hex"feedfeedfeed".bits
      val res = bytes.decode(hex"6".bits ++ b ++ tail).toOption.get
      res should_== (tail, b)
    }
  }
}
