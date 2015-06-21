package test.utils.scalacheck

import scodec.bits.BitVector
import org.scalacheck._

object Generators {
  def genLSB() = for {
    n ← Gen.choose(1, 0x7f)
  } yield BitVector(n.toByte)

  def genMSB() = for {
    n ← Gen.choose(0x80, 0xff)
    tail ← genBV()
  } yield BitVector(n.toByte) ++ tail

  def genBV(): Gen[BitVector] = Gen.resize(8, Gen.oneOf(genMSB(), genLSB()))
}
