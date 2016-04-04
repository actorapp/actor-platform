package im.actor.server.dialog

import java.time.Instant

import im.actor.server.cqrs._
import im.actor.server.dialog.DialogCommands.SendMessage
import im.actor.server.model.{ Peer, PeerType }
import im.actor.api.rpc._

import scala.concurrent.Future

trait DialogRootEvent extends TaggedEvent {
  val ts: Instant

  override def tags: Set[String] = Set("dialogRoot")
}

final case class DialogRootState(
  active:      Map[DialogGroup, Set[Peer]],
  activePeers: Set[Peer],
  archived:    Set[Peer]
) extends ProcessorState[DialogRootState, DialogRootEvent] {
  override def updated(e: DialogRootEvent): DialogRootState = ???
}

private class DialogRoot extends Processor[DialogRootState, DialogRootEvent] {
  import DialogRootEvents._

  private val userId = self.path.name.toInt
  private val selfPeer: Peer = Peer.privat(userId)

  override def persistenceId: String = s"DialogRoot_$userId"

  override protected def getInitialState: DialogRootState = DialogRootState(Map.empty, Set.empty, Set.empty)

  override protected def handleQuery: PartialFunction[Any, Future[Any]] = ???

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
    case _ ⇒
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
}