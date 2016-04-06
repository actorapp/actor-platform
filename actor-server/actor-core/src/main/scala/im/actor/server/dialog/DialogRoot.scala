package im.actor.server.dialog

import java.time.Instant

import akka.actor.{ ActorRef, Props }
import akka.pattern.ask
import akka.util.Timeout
import im.actor.concurrent._
import im.actor.server.cqrs._
import im.actor.server.dialog.DialogCommands.SendMessage
import im.actor.server.model.{ Peer, PeerType }
import im.actor.api.rpc._
import im.actor.api.rpc.misc.ApiExtension
import im.actor.config.ActorConfig

import scala.concurrent.Future

trait DialogRootEvent extends TaggedEvent {
  val ts: Instant

  override def tags: Set[String] = Set("dialogRoot")
}

trait DialogRootQuery

final case class DialogRootState(
  active:      Map[DialogGroup, Set[Peer]],
  activePeers: Set[Peer],
  archived:    Set[Peer]
) extends ProcessorState[DialogRootState, DialogRootEvent] {
  import DialogRootEvents._

  override def updated(e: DialogRootEvent): DialogRootState = e match {
    case Created(_, peer) ⇒ withNewPeer(peer)
  }

  private def withNewPeer(peer: Peer) =
    copy(
      activePeers = this.activePeers + peer,
      active = this.active + dialogGroup(peer)
    )

  private def dialogGroup(peer: Peer) = {
    val group = peer.typ match {
      case PeerType.Private ⇒ DialogGroups.Privates
      case PeerType.Group   ⇒ DialogGroups.Groups
      case _                ⇒ throw new RuntimeException("Unknown peer type")
    }

    group → this.active.getOrElse(group, Set.empty)
  }
}

object DialogRoot {
  def props(userId: Int, extensions: Seq[ApiExtension]) = Props(classOf[DialogRoot], userId, extensions)
}

private class DialogRoot(userId: Int, extensions: Seq[ApiExtension]) extends Processor[DialogRootState, DialogRootEvent] {
  import DialogRootEvents._
  import DialogRootQueries._
  import context.dispatcher

  private implicit val timeout = Timeout(ActorConfig.defaultTimeout)

  private val selfPeer: Peer = Peer.privat(userId)

  override def persistenceId: String = s"DialogRoot_$userId"

  override protected def getInitialState: DialogRootState = DialogRootState(Map.empty, Set.empty, Set.empty)

  override protected def handleQuery: PartialFunction[Any, Future[Any]] = {
    case GetCounter() ⇒
      val refs = state.activePeers.toSeq map dialogRef

      for {
        counters ← FutureExt.ftraverse(refs) { ref ⇒
          (ref ? DialogQueries.GetCounter()).mapTo[DialogQueries.GetCounterResponse] map (_.counter)
        }
      } yield GetCounterResponse(counters.sum)
  }

  override protected def handleCommand: Receive = {
    case sm: SendMessage ⇒
      needCreateDialog(sm) match {
        case Some(peer) ⇒
          persist(Created(Instant.now(), peer)) { e ⇒
            commit(e)
            handleDialogCommand(sm)
          }
        case None ⇒ handleDialogCommand(sm)
      }
    case dc: DialogCommand ⇒ handleDialogCommand(dc)
  }

  def handleDialogCommand: PartialFunction[DialogCommand, Unit] = {
    case ddc: DirectDialogCommand ⇒ dialogRef(ddc) forward ddc
    case dc: DialogCommand        ⇒ dialogRef(dc.dest) forward dc
  }

  private def needCreateDialog(sm: SendMessage): Option[Peer] = {
    val checkPeer =
      sm.origin.typ match {
        case PeerType.Group ⇒ sm.dest
        case PeerType.Private ⇒
          if (selfPeer == sm.dest) sm.origin
          else sm.dest
        case _ ⇒ throw new RuntimeException("Unknown peer type")
      }

    if (dialogExists(checkPeer)) None
    else Some(checkPeer)
  }

  private def dialogExists(peer: Peer): Boolean = state.activePeers.contains(peer)

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