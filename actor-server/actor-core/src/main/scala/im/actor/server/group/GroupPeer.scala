package im.actor.server.group

import akka.actor.{ Actor, ActorLogging, ActorSystem, Props }
import akka.persistence.SnapshotMetadata
import im.actor.server.cqrs.{ Event, ProcessorState }
import im.actor.server.dialog.{ DialogCommands, DialogExtension }

import scala.concurrent.ExecutionContext

private[group] object GroupPeerState {
  def empty = GroupPeerState(0, 0, 0)
}

private[group] case class GroupPeerState(
  lastSenderId:    Int,
  lastReceiveDate: Long,
  lastReadDate:    Long
) extends ProcessorState[GroupPeerState] {
  import GroupPeerEvents._
  override def updated(e: Event): GroupPeerState = e match {
    case LastSenderIdChanged(id)      ⇒ this.copy(lastSenderId = id)
    case LastReceiveDateChanged(date) ⇒ this.copy(lastReceiveDate = date)
    case LastReadDateChanged(date)    ⇒ this.copy(lastReadDate = date)
  }

  override def withSnapshot(metadata: SnapshotMetadata, snapshot: Any): GroupPeerState = this
}

private[group] sealed trait GroupPeerEvent extends Event

object GroupPeerEvents {
  private[group] case class LastSenderIdChanged(id: Int) extends GroupPeerEvent
  private[group] case class LastReceiveDateChanged(date: Long) extends GroupPeerEvent
  private[group] case class LastReadDateChanged(date: Long) extends GroupPeerEvent
}

private[group] object GroupPeer {
  def props(groupId: Int) = Props(classOf[GroupPeer], groupId)
}

private[group] final class GroupPeer(val groupId: Int)
  extends Actor
  with ActorLogging
  with GroupPeerCommandHandlers {

  import DialogCommands._
  import GroupPeerEvents._

  protected implicit val system: ActorSystem = context.system
  protected implicit val ec: ExecutionContext = system.dispatcher

  protected lazy val groupExt = GroupExtension(system)
  protected lazy val dialogExt = DialogExtension(system)

  def initialized(state: GroupPeerState): Receive = {
    case sm: SendMessage         ⇒ incomingMessage(state, sm)
    case mr: MessageReceived     ⇒ messageReceived(state, mr)
    case mr: MessageRead         ⇒ messageRead(state, mr)
    case sr: SetReaction         ⇒ setReaction(state, sr)
    case rr: RemoveReaction      ⇒ removeReaction(state, rr)
    case sc: LastSenderIdChanged ⇒ context become initialized(state.updated(sc))
  }

  override def receive: Receive = initialized(GroupPeerState.empty)

}
