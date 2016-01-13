package im.actor.server.sequence

import akka.actor._
import im.actor.api.rpc.sequence.WeakUpdate

object WeakUpdatesManager {

  @SerialVersionUID(1L)
  private[sequence] final case class Envelope(authId: Long, payload: Message)

  private[sequence] sealed trait Message

  @SerialVersionUID(1L)
  private[sequence] final case class PushUpdate(header: Int, serializedData: Array[Byte], reduceKey: Option[String], group: Option[String]) extends Message

  @SerialVersionUID(1L)
  private[sequence] final case class Subscribe(consumer: ActorRef, group: Option[String]) extends Message

  @SerialVersionUID(1L)
  private[sequence] final case class SubscribeAck(subscribe: Subscribe) extends Message

  @SerialVersionUID(1L)
  final case class UpdateReceived(update: WeakUpdate, reduceKey: Option[String])

  def props = Props(classOf[WeakUpdatesManager])
}

private final class WeakUpdatesManager extends Actor with ActorLogging {

  import WeakUpdatesManager._

  type ConsumerDescriptor = (ActorRef, Option[String])

  // TODO: set receive timeout

  def receive = working(Set.empty)

  def working(consumers: Set[ConsumerDescriptor]): Receive = {
    case Envelope(authId, PushUpdate(header, serializedData, reduceKey, groupOpt)) ⇒
      val event = UpdateReceived(WeakUpdate(System.currentTimeMillis(), header, serializedData), reduceKey)

      groupOpt match {
        case Some(group) ⇒
          consumers.view
            .filter(_._2.exists(_ == group))
            .map(_._1)
            .foreach(_ ! event)
        case None ⇒
          consumers.foreach(_._1 ! event)
      }
    case Envelope(_, sub @ Subscribe(consumer, groupOpt)) ⇒
      context.watch(consumer)

      context.become(working(consumers + (consumer → groupOpt)))
      sender() ! SubscribeAck(sub)

      log.debug("Consumer subscribed {}", consumer)
    case Terminated(consumer) ⇒
      context.become(working(consumers.filterNot(_._1 == consumer)))
  }
}
