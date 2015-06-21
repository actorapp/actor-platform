package im.actor.server.mtproto.codecs.transport

import org.scalatest.{ FlatSpec, Matchers }
import scodec.bits.BitVector

import im.actor.server.mtproto.transport.{ Ping, TransportPackage }

class TransportCodecsSpec extends FlatSpec with Matchers {
  it should "encode/decode" in transportPackage

  def transportPackage() = {
    val codec = TransportPackageCodec

    val tp = TransportPackage(2, Ping(BitVector.fromHex("00000000000000010000000000000002000000000000000119031701000000011108808786b9990210011a066170694b6579").get))
    val tpBytes = codec.encode(tp).require

    codec.decode(tpBytes).require.value should ===(tp)
  }
}