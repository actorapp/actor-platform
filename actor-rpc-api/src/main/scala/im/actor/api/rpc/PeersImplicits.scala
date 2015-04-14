package im.actor.api

import im.actor.api.rpc._, peers._

trait PeersImplicits {
  implicit class ExtOutPeer(outPeer: OutPeer) {
    lazy val asPeer: Peer =
      Peer(outPeer.`type`, outPeer.id)
  }

  implicit class ExtGroupOutPeer(groupOutPeer: GroupOutPeer) {
    lazy val asOutPeer: OutPeer =
      OutPeer(PeerType.Group, groupOutPeer.groupId, groupOutPeer.accessHash)
  }
}
