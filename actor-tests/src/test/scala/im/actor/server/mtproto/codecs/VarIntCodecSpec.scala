package im.actor.server.mtproto.codecs

import org.scalacheck.Prop._
import org.scalacheck._
import org.scalatest.{ FlatSpec, Matchers }
import scodec._
import scodec.bits._

object VarIntCodecProp extends Properties("VarIntCodec") {
  val integers = Gen.choose(Long.MinValue, Long.MaxValue)

  property("encode/decode") = forAll(integers) { (a: Long) ⇒
    val tail = BitVector(hex"feed")
    val buf = varint.encode(a).require ++ tail
    varint.decode(buf).require == DecodeResult(a.abs, tail)
  }
}

class VarIntCodecSpec extends FlatSpec with Matchers {
  "VarIntCodec" should "encode VarInt" in {
    varint.encode(543).require should ===(hex"9f04".bits)
    varint.encode(Int.MaxValue).require should ===(hex"ffffffff07".bits)
    varint.encode(Int.MinValue + 1).require should ===(hex"ffffffff07".bits)
  }

  it should "decode bytes to VarInt" in {
    varint.decode(hex"9f04".bits).require should ===(DecodeResult(543, BitVector.empty))
    varint.decode(hex"9f04ff".bits).require should ===(DecodeResult(543, hex"ff".bits))
    varint.decode(hex"ffffffff07".bits).require should ===(DecodeResult(Int.MaxValue, BitVector.empty))
  }
}
