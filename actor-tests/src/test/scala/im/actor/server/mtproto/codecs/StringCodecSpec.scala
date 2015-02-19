package im.actor.server.mtproto.codecs

import scodec.bits._
import org.scalacheck._
import org.scalacheck.Prop._
import scalaz._
import Scalaz._
import org.specs2.mutable.Specification

object StringCodecProp extends Properties("StringCodec") {
  property("encode/decode") = forAll { (a: String) =>
    val tail = BitVector(hex"feed")
    val buf = string.encode(a).toOption.get ++ tail
    string.decode(buf) == (tail, a).right
  }
}

class StringCodecSpec extends Specification {
  "StringCodec" should {
    "encode string" in {
      string.encode("¡™£¢∞§¶•ªº–тестtest") should_== hex"26c2a1e284a2c2a3c2a2e2889ec2a7c2b6e280a2c2aac2bae28093d182d0b5d181d18274657374".bits.right
      string.encode("") should_== hex"0".bits.right
    }

    "decode bytes to string" in {
      val res = string.decode(hex"26c2a1e284a2c2a3c2a2e2889ec2a7c2b6e280a2c2aac2bae28093d182d0b5d181d18274657374feed".bits).toOption.get
      res should_== (BitVector(hex"feed"), "¡™£¢∞§¶•ªº–тестtest")
    }
  }
}
