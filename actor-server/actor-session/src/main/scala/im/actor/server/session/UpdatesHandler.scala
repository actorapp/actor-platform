package im.actor.server.session

import scala.annotation.tailrec
import scala.collection.immutable

import akka.actor.{ ActorLogging, Props }
import akka.stream.actor._

import im.actor.server.mtproto.protocol.{ ProtoMessage, UpdateBox }
import im.actor.server.presences.{ GroupPresenceManagerRegion, PresenceManagerRegion }
import im.actor.server.sequence._

private[session] object UpdatesHandler {
  def props(authId: Long)(
    implicit
    seqUpdManagerRegion:        SeqUpdatesManagerRegion,
    weakUpdManagerRegion:       WeakUpdatesManagerRegion,
    presenceManagerRegion:      PresenceManagerRegion,
    groupPresenceManagerRegion: GroupPresenceManagerRegion
  ): Props = Props(classOf[UpdatesHandler], authId, seqUpdManagerRegion, weakUpdManagerRegion, presenceManagerRegion, groupPresenceManagerRegion)
}

private[session] class UpdatesHandler(authId: Long)(
  implicit
  seqUpdManagerRegion:        SeqUpdatesManagerRegion,
  weakUpdManagerRegion:       WeakUpdatesManagerRegion,
  presenceManagerRegion:      PresenceManagerRegion,
  groupPresenceManagerRegion: GroupPresenceManagerRegion
) extends ActorSubscriber with ActorPublisher[ProtoMessage] with ActorLogging {
  import ActorPublisherMessage._
  import ActorSubscriberMessage._
  import UpdatesConsumerMessage._

  val updatesConsumer = context.actorOf(UpdatesConsumer.props(authId, self), "updatesConsumer")

  def receive = subscriber.orElse(publisher).orElse {
    case unmatched ⇒
      log.error("Unmatched msg {}", unmatched)
  }

  // Subscriber-related

  def subscriber: Receive = {
    case OnNext(cmd: SubscribeCommand) ⇒
      cmd match {
        case SubscribeToOnline(userIds) ⇒
          updatesConsumer ! SubscribeToUserPresences(userIds.toSet)
        case SubscribeFromOnline(userIds) ⇒
          updatesConsumer ! UnsubscribeFromUserPresences(userIds.toSet)
        case SubscribeToGroupOnline(groupIds) ⇒
          updatesConsumer ! SubscribeToGroupPresences(groupIds.toSet)
        case SubscribeFromGroupOnline(groupIds) ⇒
          updatesConsumer ! UnsubscribeFromGroupPresences(groupIds.toSet)
      }
    case OnComplete ⇒
      context.stop(self)
    case OnError(cause) ⇒
      log.error(cause, "Error in upstream")
  }

  override val requestStrategy = WatermarkRequestStrategy(10) // TODO: configurable

  // Publisher-related
  private[this] var messageQueue = immutable.Queue.empty[ProtoMessage]

  def publisher: Receive = {
    case ub: UpdateBox ⇒ enqueueProtoMessage(ub)
    case Request(_)    ⇒ deliverBuf()
    case Cancel        ⇒ context.stop(self)
  }

  private def enqueueProtoMessage(message: ProtoMessage): Unit = {
    if (messageQueue.isEmpty && totalDemand > 0) {
      onNext(message)
    } else {
      messageQueue = messageQueue.enqueue(message)
      deliverBuf()
    }
  }

  @tailrec final def deliverBuf(): Unit = {
    if (isActive && totalDemand > 0)
      messageQueue.dequeueOption match {
        case Some((el, q)) ⇒
          messageQueue = q
          onNext(el)
          deliverBuf()
        case None ⇒
      }
  }
}
