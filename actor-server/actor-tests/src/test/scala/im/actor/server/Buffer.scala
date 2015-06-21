package im.actor.server

import akka.actor._
import scodec.bits._

private class Buffer extends Actor {
  import akka.io.Tcp._

  private[this] var buffer: BitVector = BitVector.empty

  def receive = {
    case Received(bits) ⇒
      buffer = buffer ++ BitVector(bits.asByteBuffer)
  }
}