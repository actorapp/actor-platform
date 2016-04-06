package im.actor.server.user

import java.time.{ Instant, Period }

import akka.actor.{ ActorRef, Props }
import im.actor.api.rpc.PeersImplicits
import im.actor.api.rpc.misc.ApiExtension
import im.actor.concurrent.{ AlertingActor, FutureExt }
import im.actor.server.dialog._
import im.actor.server.model.{ DialogObsolete, Peer, PeerType }

import scala.concurrent.duration._
/*
private[user] object UserPeer {
  def props(userId: Int, extensions: Seq[ApiExtension]) = Props(classOf[UserPeer], userId, extensions)

  private case object StartArchiving
  private case class ArchiveIfExpired(dialogs: Seq[Dialog])
  private case class Archive(peer: Peer)
}

private[user] final class UserPeer(userId: Int, extensions: Seq[ApiExtension]) extends AlertingActor with PeersImplicits {
  import UserPeer._
  import context.dispatcher

  private val selfPeer = Peer.privat(userId)

  private val dialogExt = DialogExtension(context.system)
  private val archiveInterval = context.system.scheduler.schedule(0.seconds, 1.hour, self, StartArchiving)

  override def postStop(): Unit = {
    super.postStop()
    archiveInterval.cancel()
  }

  def receive: Receive = {
    // Forward to a group or a corresponding user dialog
    case dc: DirectDialogCommand   ⇒ dialogRef(dc) forward dc
    // Forward to a dest user dialog
    case dc: DialogCommand         ⇒ dialogRef(dc.dest) forward dc
    case StartArchiving            ⇒ startArchiving()
    case ArchiveIfExpired(dialogs) ⇒ archiveIfExpired(dialogs)
    case Archive(peer)             ⇒ archive(peer)
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

  private def startArchiving(): Unit = {
    for {
      dialogs ← dialogExt.fetchGroupedDialogs(userId) map (_ filterNot (_._1 == DialogGroups.Favourites) flatMap (_._2))
    } if (dialogs.size > 12)
      self ! ArchiveIfExpired(dialogs.toList)
  }

  private def archiveIfExpired(dialogs: Seq[Dialog]): Unit = {
    FutureExt.ftraverse(dialogs) { dialog ⇒
      for (short ← dialogExt.getDialogShort(dialog)) yield {
        if (short.counter == 0 && short.date <= Instant.now().minus(Period.ofDays(5)).toEpochMilli) {
          Some(short.peer.asModel)
        } else None
      }
    } foreach (_.flatten foreach (self ! Archive(_)))
  }

  private def archive(peer: Peer): Unit = {
    log.debug("Archiving dialog {} due to inactivity", peer)
    dialogRef(peer) ! DialogCommands.Archive(peer)
  }

}
*/ 