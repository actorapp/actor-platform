package im.actor.server.mtproto.codecs.transport

import scala.util.Random

import org.specs2.Specification
import org.specs2.matcher.ThrownExpectations
import scodec.bits.BitVector

import im.actor.server.mtproto.transport.{ Ping, MTPackage, TransportPackage }

class TransportCodecsSpec extends Specification with ThrownExpectations {
  def is = s2"""
             TransportPackageCodec should encode/decode $transportPackage
             """

  def transportPackage = {
    val codec = TransportPackageCodec

    val tp = TransportPackage(2, Ping(BitVector.fromHex("00000000000000010000000000000002000000000000000119031701000000011108808786b9990210011a066170694b6579").get))
    val tpBytes = codec.encode(tp).require

    codec.decode(tpBytes).require.value should_==(tp)
  }
}