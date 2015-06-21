package im.actor.server.mtproto.codecs

import org.scalacheck.Prop._
import org.scalacheck._
import org.scalatest.{ FlatSpec, Matchers }
import scodec._
import scodec.bits._

object StringCodecProp extends Properties("StringCodec") {
  property("encode/decode") = forAll { (a: String) ⇒
    val tail = BitVector(hex"feed")
    val buf = string.encode(a).require ++ tail
    string.decode(buf).require == DecodeResult(a, tail)
  }
}

class StringCodecSpec extends FlatSpec with Matchers {
  "StringCodec" should "encode string" in {
    string.encode("¡™£¢∞§¶•ªº–тестtest").require should ===(hex"26c2a1e284a2c2a3c2a2e2889ec2a7c2b6e280a2c2aac2bae28093d182d0b5d181d18274657374".bits)
    string.encode("").require should ===(hex"0".bits)
  }

  it should "decode bytes to string" in {
    val res = string.decode(hex"26c2a1e284a2c2a3c2a2e2889ec2a7c2b6e280a2c2aac2bae28093d182d0b5d181d18274657374feed".bits)
    res.require should ===(DecodeResult("¡™£¢∞§¶•ªº–тестtest", BitVector(hex"feed")))
  }
}
