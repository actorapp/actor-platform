package im.actor.api.rpc

import im.actor.api.rpc.peers.{ ApiGroupOutPeer, ApiOutPeer, ApiPeer, ApiPeerType }
import im.actor.server.model.{ Peer, PeerType }

trait PeersImplicits {

  implicit class ExtPeer(peer: ApiPeer) {
    lazy val asModel: Peer =
      Peer(PeerType.fromValue(peer.`type`.id), peer.id)
  }

  implicit class ExtOutPeer(outPeer: ApiOutPeer) {
    lazy val asPeer: ApiPeer =
      ApiPeer(outPeer.`type`, outPeer.id)

    lazy val asModel: Peer =
      Peer(PeerType.fromValue(outPeer.`type`.id), outPeer.id)
  }

  implicit class ExtGroupOutPeer(groupOutPeer: ApiGroupOutPeer) {
    lazy val asOutPeer: ApiOutPeer =
      ApiOutPeer(ApiPeerType.Group, groupOutPeer.groupId, groupOutPeer.accessHash)

    lazy val asPeer: ApiPeer =
      ApiPeer(ApiPeerType.Group, groupOutPeer.groupId)

    lazy val asModel: Peer =
      Peer(PeerType.Group, groupOutPeer.groupId)
  }

  implicit class ExtPeerModel(model: Peer) {
    lazy val asStruct: ApiPeer =
      ApiPeer(ApiPeerType(model.typ.value), model.id)
  }

  implicit class ExtPeerCompanion(companion: com.trueaccord.scalapb.GeneratedMessageCompanion[Peer]) {
    def privat(userId: Int) = Peer(PeerType.Private, userId)
    def group(groupId: Int) = Peer(PeerType.Group, groupId)
  }

}
