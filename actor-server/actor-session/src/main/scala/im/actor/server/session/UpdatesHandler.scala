package im.actor.server.session

import akka.actor.{ ActorLogging, Props }
import akka.stream.actor._
import im.actor.server.mtproto.protocol.UpdateBox
import im.actor.server.sequence._

import scala.annotation.tailrec
import scala.collection.immutable

private[session] object UpdatesHandler {
  def props(authId: Long)(implicit seqUpdManagerRegion: SeqUpdatesManagerRegion): Props =
    Props(classOf[UpdatesHandler], authId, seqUpdManagerRegion)
}

private[session] class UpdatesHandler(authId: Long)(implicit seqUpdManagerRegion: SeqUpdatesManagerRegion)
  extends ActorSubscriber with ActorPublisher[(UpdateBox, Option[String])] with ActorLogging {

  import ActorPublisherMessage._
  import ActorSubscriberMessage._
  //import UpdatesConsumerMessage._

  private val updatesConsumer = context.actorOf(UpdatesConsumer.props(authId, self), "updatesConsumer")

  def receive = subscriber.orElse(publisher).orElse {
    case unmatched ⇒
      log.error("Unmatched msg {}", unmatched)
  }

  // Subscriber-related

  def subscriber: Receive = {
    case OnNext(cmd: SubscribeCommand) ⇒
      cmd match {
        case SubscribeToOnline(userIds) ⇒
          updatesConsumer ! UpdatesConsumerMessage.SubscribeToUserPresences(userIds.toSet)
        case SubscribeFromOnline(userIds) ⇒
          updatesConsumer ! UpdatesConsumerMessage.UnsubscribeFromUserPresences(userIds.toSet)
        case SubscribeToGroupOnline(groupIds) ⇒
          updatesConsumer ! UpdatesConsumerMessage.SubscribeToGroupPresences(groupIds.toSet)
        case SubscribeFromGroupOnline(groupIds) ⇒
          updatesConsumer ! UpdatesConsumerMessage.UnsubscribeFromGroupPresences(groupIds.toSet)
        case SubscribeToSeq() ⇒
          updatesConsumer ! UpdatesConsumerMessage.SubscribeToSeq
      }
    case OnComplete ⇒
      context.stop(self)
    case OnError(cause) ⇒
      log.error(cause, "Error in upstream")
  }

  override val requestStrategy = WatermarkRequestStrategy(10) // TODO: configurable

  // Publisher-related
  private[this] var messageQueue = immutable.Queue.empty[(UpdateBox, Option[String])]

  def publisher: Receive = {
    case NewUpdate(ub, reduceKey) ⇒ enqueueProtoMessage(ub, reduceKey)
    case Request(_)               ⇒ deliverBuf()
    case Cancel                   ⇒ context.stop(self)
  }

  private def enqueueProtoMessage(message: UpdateBox, reduceKey: Option[String]): Unit = {
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
