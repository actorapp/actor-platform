package im.actor.server.model

trait DialogId {
  val typ: PeerType
  val id: String
}

final class PrivateDialogId(_id1: Int, _id2: Int) extends DialogId {
  override val typ: PeerType = PeerType.Private

  val (id1, id2) =
    if (_id1 < _id2) (_id1, _id2)
    else if (_id1 > _id2) (_id2, _id1)
    else throw new IllegalArgumentException("Private dialog is impossible with itself")

  override val id: String = s"${id1}_$id2"
}

final case class GroupDialogId(groupId: Int) extends DialogId {
  override val typ: PeerType = PeerType.Group
  override val id: String = groupId.toString
}

object DialogId {
  def privat(id1: Int, id2: Int): DialogId = new PrivateDialogId(id1, id2)

  def group(id: Int): DialogId = GroupDialogId(id)

  def apply(peer: Peer, clientUserId: Int): DialogId =
    peer.typ match {
      case PeerType.Private ⇒ privat(peer.id, clientUserId)
      case PeerType.Group   ⇒ group(peer.id)
      case _                ⇒ throw new IllegalArgumentException(s"Wrong peer type ${peer.typ}")
    }
}