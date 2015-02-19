package im.actor.server.mtproto.codecs

import scodec.bits._
import scodec._
import org.scalacheck._
import org.scalacheck.Prop._
import org.specs2.mutable.Specification

object StringCodecProp extends Properties("StringCodec") {
  property("encode/decode") = forAll { (a: String) =>
    val tail = BitVector(hex"feed")
    val buf = string.encode(a).require ++ tail
    string.decode(buf).require == DecodeResult(a, tail)
  }
}

class StringCodecSpec extends Specification {
  "StringCodec" should {
    "encode string" in {
      string.encode("¡™£¢∞§¶•ªº–тестtest").require should_== hex"26c2a1e284a2c2a3c2a2e2889ec2a7c2b6e280a2c2aac2bae28093d182d0b5d181d18274657374".bits
      string.encode("").require should_== hex"0".bits
    }

    "decode bytes to string" in {
      val res = string.decode(hex"26c2a1e284a2c2a3c2a2e2889ec2a7c2b6e280a2c2aac2bae28093d182d0b5d181d18274657374feed".bits)
      res.require should_== DecodeResult("¡™£¢∞§¶•ªº–тестtest", BitVector(hex"feed"))
    }
  }
}
