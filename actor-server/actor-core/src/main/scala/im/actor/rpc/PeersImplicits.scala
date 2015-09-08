package im.actor.api.rpc

import im.actor.api.rpc.peers._
import im.actor.server.models

trait PeersImplicits {

  implicit class ExtPeer(peer: ApiPeer) {
    lazy val asModel: models.Peer =
      models.Peer(models.PeerType.fromInt(peer.`type`.id), peer.id)
  }

  implicit class ExtOutPeer(outPeer: ApiOutPeer) {
    lazy val asPeer: ApiPeer =
      ApiPeer(outPeer.`type`, outPeer.id)

    lazy val asModel: models.Peer =
      models.Peer(models.PeerType.fromInt(outPeer.`type`.id), outPeer.id)
  }

  implicit class ExtGroupOutPeer(groupOutPeer: ApiGroupOutPeer) {
    lazy val asOutPeer: ApiOutPeer =
      ApiOutPeer(ApiPeerType.Group, groupOutPeer.groupId, groupOutPeer.accessHash)

    lazy val asPeer: ApiPeer =
      ApiPeer(ApiPeerType.Group, groupOutPeer.groupId)
  }

  implicit class ExtPeerModel(model: models.Peer) {
    lazy val asStruct: ApiPeer =
      ApiPeer(ApiPeerType(model.typ.toInt), model.id)
  }
}
