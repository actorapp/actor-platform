package im.actor.server.mtproto.codecs.transport

import scala.util.Random

import org.specs2.Specification
import org.specs2.matcher.ThrownExpectations
import scodec.bits.BitVector

import im.actor.server.mtproto.transport._
/*
class HandshakeCodecsSpec extends Specification with ThrownExpectations {
  def is = s2"""
             HandshakeCodec should encode/decode $e1
             """
             //Handshake util codecs should decode HandshakeCodec-encoded data $e2
             //"""

  def e1 = {
    val codec = handshakeResponse

    val h = Handshake(1.toByte, 2.toByte, 3.toByte, BitVector(1, 2, 3, 4, 5, 6, 7, 8))
    val hBytes = codec.encode(h).require

    codec.decode(hBytes).require.value should_==(h)
  }

  def e2 = {
    val codec = handshake

    val hsData = BitVector(1, 2, 3, 4, 5, 6, 7, 8)

    val h = Handshake(1.toByte, 2.toByte, 3.toByte, hsData)
    val hBytes = codec.encode(h).require

    val headerRes = handshakeHeader.decode(hBytes).require
    headerRes.value should_==(HandshakeHeader(1.toByte, 2.toByte, 3.toByte, 8))

    val dataRes = handshakeData(headerRes.value.dataLength).decode(headerRes.remainder).require

    dataRes.remainder should_==(BitVector.empty)
    dataRes.value should_==(hsData)
  }
}
*/