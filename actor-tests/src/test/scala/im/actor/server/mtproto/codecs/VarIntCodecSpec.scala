package im.actor.server.mtproto.codecs

import scodec.bits._
import scodec._
import org.scalacheck._
import org.scalacheck.Prop._
import org.specs2.mutable.Specification

object VarIntCodecProp extends Properties("VarIntCodec") {
  val integers = Gen.choose(Long.MinValue, Long.MaxValue)

  property("encode/decode") = forAll(integers) { (a: Long) =>
    val tail = BitVector(hex"feed")
    val buf = varint.encode(a).require ++ tail
    varint.decode(buf).require == DecodeResult(a.abs, tail)
  }
}

class VarIntCodecSpec extends Specification {
  "VarIntCodec" should {
    "encode VarInt" in {
      varint.encode(543).require should_== hex"9f04".bits
      varint.encode(Int.MaxValue).require should_== hex"ffffffff07".bits
      varint.encode(Int.MinValue + 1).require should_== hex"ffffffff07".bits
    }

    "decode bytes to VarInt" in {
      varint.decode(hex"9f04".bits).require should_== DecodeResult(543, BitVector.empty)
      varint.decode(hex"9f04ff".bits).require should_== DecodeResult(543, hex"ff".bits)
      varint.decode(hex"ffffffff07".bits).require should_== DecodeResult(Int.MaxValue, BitVector.empty)
    }
  }
}
