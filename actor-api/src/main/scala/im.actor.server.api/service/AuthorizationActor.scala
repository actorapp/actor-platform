package im.actor.server.api.service

import akka.actor.Actor
import im.actor.server.api.mtproto.transport._
import scodec.bits.BitVector

class AuthorizationActor extends Actor {
  def receive = {
    case pkg @ MTPackage(_, _, m) =>
      if (m.startsWith(BitVector("q".getBytes))) sender() ! SilentClose
      else sender() ! pkg
  }
}
