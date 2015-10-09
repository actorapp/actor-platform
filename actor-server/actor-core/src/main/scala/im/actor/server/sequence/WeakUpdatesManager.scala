package im.actor.server.sequence

import akka.actor._
import im.actor.api.rpc.sequence.WeakUpdate

object WeakUpdatesManager {

  @SerialVersionUID(1L)
  private[sequence] case class Envelope(authId: Long, payload: Message)

  private[sequence] sealed trait Message

  @SerialVersionUID(1L)
  private[sequence] case class PushUpdate(header: Int, serializedData: Array[Byte], reduceKey: Option[String]) extends Message

  @SerialVersionUID(1L)
  private[sequence] case class Subscribe(consumer: ActorRef) extends Message

  @SerialVersionUID(1L)
  private[sequence] case class SubscribeAck(consumer: ActorRef) extends Message

  @SerialVersionUID(1L)
  case class UpdateReceived(update: WeakUpdate, reduceKey: Option[String])

  def props = Props(classOf[WeakUpdatesManager])
}

class WeakUpdatesManager extends Actor with ActorLogging {

  import WeakUpdatesManager._

  // TODO: set receive timeout

  def receive = working(Set.empty)

  def working(consumers: Set[ActorRef]): Receive = {
    case Envelope(authId, PushUpdate(header, serializedData, reduceKey)) ⇒
      consumers foreach (_ ! UpdateReceived(WeakUpdate(System.currentTimeMillis(), header, serializedData), reduceKey))
    case Envelope(_, Subscribe(consumer)) ⇒
      context.watch(consumer)
      context.become(working(consumers + consumer))
      sender() ! SubscribeAck(consumer)

      log.debug("Consumer subscribed {}", consumer)
    case Terminated(consumer) ⇒
      context.become(working(consumers - consumer))
  }
}
