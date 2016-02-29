package im.actor.server.user

import java.time.{ Instant, Period }

import akka.actor.{ ActorRef, Props }
import im.actor.api.rpc.PeersImplicits
import im.actor.api.rpc.misc.ApiExtension
import im.actor.concurrent.AlertingActor
import im.actor.server.dialog._
import im.actor.server.model.{ Peer, PeerType }

import scala.concurrent.duration._

private[user] object UserPeer {
  def props(userId: Int, extensions: Seq[ApiExtension]) = Props(classOf[UserPeer], userId, extensions)

  private case object Archive
}

private[user] final class UserPeer(userId: Int, extensions: Seq[ApiExtension]) extends AlertingActor with PeersImplicits {
  import UserPeer._
  import context.dispatcher

  private val selfPeer = Peer.privat(userId)

  private val dialogExt = DialogExtension(context.system)
  private val archiveInterval = context.system.scheduler.schedule(0.seconds, 1.hour, self, Archive)

  override def postStop(): Unit = {
    super.postStop()
    archiveInterval.cancel()
  }

  def receive: Receive = {
    // Forward to a group or a corresponding user dialog
    case dc: DirectDialogCommand ⇒ dialogRef(dc) forward dc
    // Forward to a dest user dialog
    case dc: DialogCommand       ⇒ dialogRef(dc.dest) forward dc
    case Archive                 ⇒ archive()
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

  private def archive(): Unit = {
    for {
      dialogs ← dialogExt.getGroupedDialogs(userId) map (_ filterNot (_.key == DialogGroups.Favourites.key) flatMap (_.dialogs))
    } yield {
      val toArchive = dialogs filter (d ⇒ d.counter == 0 && d.date <= Instant.now().minus(Period.ofDays(5)).toEpochMilli)
      for (dialog ← toArchive) {
        log.debug("Archiving dialog {} due to inactivity", dialog.peer)
        val command = DialogCommands.Archive(dialog.peer.asModel)
        dialogRef(command.dest) ! command
      }
    }
  }

}
