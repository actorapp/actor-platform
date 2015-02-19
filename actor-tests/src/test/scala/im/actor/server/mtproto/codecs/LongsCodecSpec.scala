package im.actor.server.mtproto.codecs

import scodec.bits._
import scodec._
import org.scalacheck._
import org.scalacheck.Prop._
import org.specs2.mutable.Specification

object LongsCodecProp extends Properties("LongsCodec") {
  def genLong() = for {
    n <- Gen.choose(Long.MinValue, Long.MaxValue)
    tail <- genLongs()
  } yield Vector(n) ++ tail

  def genLongs(): Gen[Vector[Long]] = Gen.oneOf(genLong(), Gen.const(Vector[Long]()))

  property("encode/decode") = forAll(genLongs()) { (v: Vector[Long]) =>
    val tail = BitVector(hex"feed")
    val buf = longs.encode(v).require ++ tail
    longs.decode(buf).require == DecodeResult(v, tail)
  }
}

class LongsCodecSpec extends Specification {
  "LongsCodec" should {
    "encode array of longs" in {
      longs.encode(Vector(200L, Long.MaxValue)).require should_== hex"0200000000000000c87fffffffffffffff".bits
      longs.encode(Vector[Long]()).require should_== hex"0".bits
    }

    "decode bytes to array of longs" in {
      val res = longs.decode(hex"0200000000000000c87ffffffffffffffffeed".bits)
      res.require should_== DecodeResult(Vector(200L, Long.MaxValue), BitVector(hex"feed"))
    }
  }
}
