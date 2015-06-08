package im.actor.server.api.rpc.service.ilectro

import java.util.UUID

import im.actor.api.rpc.peers.Peer
import im.actor.api.rpc.peers.PeerType._

object InterceptorsCommon {
  private[ilectro] case object ResetCountdown
  private[ilectro] case object Resubscribe

  private[ilectro] case class AdsUser(userId: Int, ilectroUUID: UUID)

  private[ilectro] def interceptorGroupId(peer: Peer): String = {
    peer match {
      case Peer(Group, id)   ⇒ s"group-$id"
      case Peer(Private, id) ⇒ s"private-$id"
    }
  }
}
