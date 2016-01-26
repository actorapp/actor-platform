package im.actor.server.session

import akka.actor.{ Stash, ActorRef, ActorLogging, Props }
import akka.stream.actor._
import im.actor.server.mtproto.protocol.ProtoPush
import im.actor.server.sequence._

import scala.annotation.tailrec
import scala.collection.immutable

private[session] object UpdatesHandler {
  final case class Authorize(userId: Int, authSid: Int)

  def props(authId: Long): Props =
    Props(classOf[UpdatesHandler], authId)
}

private[session] class UpdatesHandler(authId: Long)
  extends ActorSubscriber with ActorPublisher[(ProtoPush, Option[String])] with ActorLogging with Stash {

  import ActorPublisherMessage._
  import ActorSubscriberMessage._
  //import UpdatesConsumerMessage._

  def receive = {
    case UpdatesHandler.Authorize(userId, authSid) ⇒
      val updatesConsumer = context.actorOf(UpdatesConsumer.props(userId, authId, authSid, self), "updates-consumer")

      context become authorized(updatesConsumer)
    case msg ⇒ stash()
  }

  def authorized(consumer: ActorRef): Receive = subscriber(consumer) orElse publisher orElse {
    case unmatched ⇒ log.error("Unmatched msg: {}", unmatched)
  }

  // Subscriber-related

  def subscriber(consumer: ActorRef): Receive = {
    case OnNext(cmd: SubscribeCommand) ⇒
      cmd match {
        case SubscribeToOnline(userIds) ⇒
          consumer ! UpdatesConsumerMessage.SubscribeToUserPresences(userIds.toSet)
        case SubscribeFromOnline(userIds) ⇒
          consumer ! UpdatesConsumerMessage.UnsubscribeFromUserPresences(userIds.toSet)
        case SubscribeToGroupOnline(groupIds) ⇒
          consumer ! UpdatesConsumerMessage.SubscribeToGroupPresences(groupIds.toSet)
        case SubscribeFromGroupOnline(groupIds) ⇒
          consumer ! UpdatesConsumerMessage.UnsubscribeFromGroupPresences(groupIds.toSet)
        case SubscribeToSeq() ⇒
          consumer ! UpdatesConsumerMessage.SubscribeToSeq
        case SubscribeToWeak(Some(group)) ⇒
          consumer ! UpdatesConsumerMessage.SubscribeToWeak(Some(group))
        case SubscribeToWeak(None) ⇒
          log.error("Subscribe to weak is done implicitly on UpdatesConsumer start")
      }
    case OnComplete ⇒
      context.stop(self)
    case OnError(cause) ⇒
      log.error(cause, "Error in upstream")
  }

  override val requestStrategy = WatermarkRequestStrategy(10) // TODO: configurable

  // Publisher-related
  private[this] var messageQueue = immutable.Queue.empty[(ProtoPush, Option[String])]

  def publisher: Receive = {
    case NewUpdate(ub, reduceKey) ⇒ enqueueProtoMessage(ub, reduceKey)
    case Request(_)               ⇒ deliverBuf()
    case Cancel                   ⇒ context.stop(self)
  }

  private def enqueueProtoMessage(message: ProtoPush, reduceKey: Option[String]): Unit = {
    if (messageQueue.isEmpty && totalDemand > 0) {
      onNext(message → reduceKey)
    } else {
      messageQueue = messageQueue.enqueue(message → reduceKey)
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
