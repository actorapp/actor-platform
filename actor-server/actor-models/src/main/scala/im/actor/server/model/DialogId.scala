package im.actor.server.model

final case class DialogId(typ: PeerType, id: String)

object DialogId {
  def privat(id1: Int, id2: Int): DialogId = DialogId(PeerType.Private, id(id1, id2))

  def group(id: Int): DialogId = DialogId(PeerType.Group, id.toString)

  def apply(peer: Peer, clientUserId: Int): DialogId =
    peer.typ match {
      case PeerType.Private ⇒ privat(peer.id, clientUserId)
      case PeerType.Group   ⇒ group(peer.id)
      case _                ⇒ throw new RuntimeException(s"Wrong peer type ${peer.typ}")
    }

  private def id(id1: Int, id2: Int) =
    if (id1 < id2)
      s"${id1}_$id2"
    else
      s"${id2}_$id1"
}