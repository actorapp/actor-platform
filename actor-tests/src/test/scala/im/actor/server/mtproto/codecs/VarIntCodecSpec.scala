package im.actor.server.mtproto.codecs

import scodec.bits._
import org.scalacheck._
import org.scalacheck.Prop._
import scalaz._
import Scalaz._
import org.specs2.mutable.Specification

object VarIntCodecProp extends Properties("VarIntCodec") {
  val integers = Gen.choose(Long.MinValue, Long.MaxValue)

  property("encode/decode") = forAll(integers) { (a: Long) =>
    val tail = BitVector(hex"feed")
    val buf = varint.encode(a).toOption.get ++ tail
    varint.decode(buf) == (tail, a.abs).right
  }
}

class VarIntCodecSpec extends Specification {
  "VarIntCodec" should {
    "encode VarInt" in {
      varint.encode(543) should_== hex"9f04".bits.right
      varint.encode(Int.MaxValue) should_== hex"ffffffff07".bits.right
      varint.encode(Int.MinValue + 1) should_== hex"ffffffff07".bits.right
    }

    "decode bytes to VarInt" in {
      varint.decode(hex"9f04".bits) should_== (BitVector.empty, 543).right
      varint.decode(hex"9f04ff".bits) should_== (hex"ff".bits, 543).right
      varint.decode(hex"ffffffff07".bits) should_== (BitVector.empty, Int.MaxValue).right
    }
  }
}
