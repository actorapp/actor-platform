package im.actor.server.group

import akka.actor.{ ActorSystem, ActorLogging, Actor, Props }
import im.actor.server.cqrs.ProcessorState
import im.actor.server.dialog.{ DialogExtension, DialogCommands }

import scala.concurrent.ExecutionContext

private[group] object GroupPeerState {
  def empty = GroupPeerState(0, 0, 0)
}

private[group] case class GroupPeerState(
  lastSenderId:    Int,
  lastReceiveDate: Long,
  lastReadDate:    Long
) extends ProcessorState[GroupPeerState, GroupPeerEvent] {
  import GroupPeerEvents._
  override def updated(e: GroupPeerEvent): GroupPeerState = e match {
    case LastSenderIdChanged(id)      ⇒ this.copy(lastSenderId = id)
    case LastReceiveDateChanged(date) ⇒ this.copy(lastReceiveDate = date)
    case LastReadDateChanged(date)    ⇒ this.copy(lastReadDate = date)
  }
}

private[group] sealed trait GroupPeerEvent

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
    case uc: UpdateCounters      ⇒ updateCountersChanged(uc)
  }

  override def receive: Receive = initialized(GroupPeerState.empty)

}
