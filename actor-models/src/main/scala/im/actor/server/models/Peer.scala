package im.actor.server.models

@SerialVersionUID(1L)
case class Peer(typ: PeerType, id: Int)

object Peer {
  def privat(userId: Int) = Peer(PeerType.Private, userId)

  def group(groupId: Int) = Peer(PeerType.Group, groupId)
}
