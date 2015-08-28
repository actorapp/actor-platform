package im.actor.api.rpc

import im.actor.api.rpc.peers._
import im.actor.server.models

trait PeersImplicits {

  implicit class ExtPeer(peer: Peer) {
    lazy val asModel: models.Peer =
      models.Peer(models.PeerType.fromInt(peer.`type`.id), peer.id)
  }

  implicit class ExtOutPeer(outPeer: OutPeer) {
    lazy val asPeer: Peer =
      Peer(outPeer.`type`, outPeer.id)

    lazy val asModel: models.Peer =
      models.Peer(models.PeerType.fromInt(outPeer.`type`.id), outPeer.id)
  }

  implicit class ExtGroupOutPeer(groupOutPeer: GroupOutPeer) {
    lazy val asOutPeer: OutPeer =
      OutPeer(PeerType.Group, groupOutPeer.groupId, groupOutPeer.accessHash)

    lazy val asPeer: Peer =
      Peer(PeerType.Group, groupOutPeer.groupId)
  }

  implicit class ExtPeerModel(model: models.Peer) {
    lazy val asStruct: Peer =
      Peer(PeerType(model.typ.toInt), model.id)
  }
}
