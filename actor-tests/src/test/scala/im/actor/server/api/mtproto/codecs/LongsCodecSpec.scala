package im.actor.server.api.mtproto.codecs

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
