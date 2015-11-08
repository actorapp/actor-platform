package im.actor.server.group

import java.time.Instant

import akka.actor.{ ActorSystem, ActorLogging, Actor, Props }
import im.actor.concurrent.ActorFutures
import im.actor.server.cqrs.ProcessorState
import im.actor.server.dialog.{ DialogExtension, ActorDelivery, DialogCommands }
import im.actor.server.model.Peer

import scala.concurrent.ExecutionContext

case class GroupPeerState(
  lastSenderId:    Option[Int],
  lastReceiveDate: Option[Long],
  lastReadDate:    Option[Long]
) extends ProcessorState[GroupPeerState] {
  import GroupPeerEvents._
  override def updated(e: AnyRef, ts: Instant): GroupPeerState = e match {
    case LastSenderIdChanged(id)      ⇒ this.copy(lastSenderId = Some(id))
    case LastReceiveDateChanged(date) ⇒ this.copy(lastReceiveDate = Some(date))
    case LastReadDateChanged(date)    ⇒ this.copy(lastReadDate = Some(date))
  }
}

object GroupPeerEvents {
  private[group] sealed trait GroupPeerEvent
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
  with GroupPeerCommandHandlers
  with ActorFutures {
  import DialogCommands._

  protected implicit val system: ActorSystem = context.system
  protected implicit val ec: ExecutionContext = system.dispatcher

  protected lazy val groupExt = GroupExtension(system)
  protected lazy val dialogExt = DialogExtension(system)

  def initialized(state: GroupPeerState): Receive = {
    case sm: SendMessage     ⇒ incomingMessage(state, sm)
    case mr: MessageReceived ⇒ messageReceived(state, mr)
    case mr: MessageRead     ⇒ messageRead(state, mr)
  }

  override def receive: Receive = initialized(GroupPeerState(None, None, None))

}
