package im.actor.server.mtproto.codecs

import scodec.bits._
import org.scalacheck._
import org.scalacheck.Prop._
import scalaz._
import Scalaz._
import org.specs2.mutable.Specification

object LongsCodecProp extends Properties("LongsCodec") {
  def genLong() = for {
    n <- Gen.choose(Long.MinValue, Long.MaxValue)
    tail <- genLongs()
  } yield Vector(n) ++ tail

  def genLongs(): Gen[Vector[Long]] = Gen.oneOf(genLong(), Gen.const(Vector[Long]()))

  property("encode/decode") = forAll(genLongs()) { (v: Vector[Long]) =>
    val tail = BitVector(hex"feed")
    val buf = longs.encode(v).toOption.get ++ tail
    longs.decode(buf) == (tail, v).right
  }
}

class LongsCodecSpec extends Specification {
  "LongsCodec" should {
    "encode array of longs" in {
      longs.encode(Vector(200L, Long.MaxValue)) should_== hex"0200000000000000c87fffffffffffffff".bits.right
      longs.encode(Vector[Long]()) should_== hex"0".bits.right
    }

    "decode bytes to array of longs" in {
      val res = longs.decode(hex"0200000000000000c87ffffffffffffffffeed".bits)
      res should_== (BitVector(hex"feed"), Vector(200L, Long.MaxValue)).right
    }
  }
}
