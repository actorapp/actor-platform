package im.actor.server.user

import akka.actor.{ActorLogging, Actor, ActorRef, Props}
import im.actor.api.rpc.PeersImplicits
import im.actor.api.rpc.misc.ApiExtension
import im.actor.server.dialog.{ DirectDialogCommand, DialogProcessor, DialogCommand }
import im.actor.server.model.{ Peer, PeerType }

private[user] object UserPeer {
  def props(userId: Int, extensions: Seq[ApiExtension]) = Props(classOf[UserPeer], userId, extensions)
}

private[user] final class UserPeer(userId: Int, extensions: Seq[ApiExtension]) extends Actor with ActorLogging with PeersImplicits {

  private val selfPeer = Peer.privat(userId)

  def receive: Receive = {
    // Forward to a group or a corresponding user dialog
    case dc: DirectDialogCommand ⇒ dialogRef(dc) forward dc
    // Forward to a dest user dialog
    case dc: DialogCommand       ⇒ dialogRef(dc.dest) forward dc
    case other                   ⇒ log.debug("Unmatched message: {}", other)
  }

  private def dialogRef(dc: DirectDialogCommand): ActorRef = {
    val peer = dc.dest match {
      case Peer(PeerType.Group, _)   ⇒ dc.dest
      case Peer(PeerType.Private, _) ⇒ if (dc.origin == selfPeer) dc.dest else dc.origin
    }
    dialogRef(peer)
  }

  private def dialogRef(peer: Peer): ActorRef =
    context.child(dialogName(peer)) getOrElse context.actorOf(DialogProcessor.props(userId, peer, extensions), dialogName(peer))

  private def dialogName(peer: Peer): String = peer.typ match {
    case PeerType.Private ⇒ s"Private_${peer.id}"
    case PeerType.Group   ⇒ s"Group_${peer.id}"
    case other            ⇒ throw new Exception(s"Unknown peer type: $other")
  }

}
