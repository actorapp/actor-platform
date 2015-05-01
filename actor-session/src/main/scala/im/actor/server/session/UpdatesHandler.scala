package im.actor.server.session

import scala.annotation.tailrec
import scala.collection.immutable

import akka.actor.{ ActorLogging, Props }
import akka.stream.actor._

import im.actor.server.mtproto.protocol.{ ProtoMessage, UpdateBox }
import im.actor.server.presences.PresenceManagerRegion
import im.actor.server.push.{ SeqUpdatesManagerRegion, UpdatesPusher, UpdatesPusherMessage, WeakUpdatesManagerRegion }
import im.actor.server.session.SessionMessage._

private[session] object UpdatesHandler {
  def props(authId: Long)(
    implicit
    seqUpdManagerRegion:   SeqUpdatesManagerRegion,
    weakUpdManagerRegion:  WeakUpdatesManagerRegion,
    presenceManagerRegion: PresenceManagerRegion
  ): Props = Props(classOf[UpdatesHandler], authId, seqUpdManagerRegion, weakUpdManagerRegion, presenceManagerRegion)
}

private[session] class UpdatesHandler(authId: Long)(
  implicit
  seqUpdManagerRegion:   SeqUpdatesManagerRegion,
  weakUpdManagerRegion:  WeakUpdatesManagerRegion,
  presenceManagerRegion: PresenceManagerRegion
) extends ActorSubscriber with ActorPublisher[ProtoMessage] with ActorLogging {
  import ActorPublisherMessage._
  import ActorSubscriberMessage._

  val updatesPusher = context.actorOf(UpdatesPusher.props(authId, self))

  def receive = subscriber.orElse(publisher).orElse {
    case unmatched ⇒
      log.error("Unmatched msg {}", unmatched)
  }

  // Subscriber-related

  def subscriber: Receive = {
    case OnNext(cmd: SubscribeCommand) ⇒
      cmd match {
        case SubscribeToOnline(userIds) ⇒
          updatesPusher ! UpdatesPusherMessage.SubscribeToUserPresences(userIds)
        case SubscribeFromOnline(userIds) ⇒
          updatesPusher ! UpdatesPusherMessage.UnsubscribeFromUserPresences(userIds)
        case SubscribeToGroupOnline(groupIds)   ⇒
        // FIXME: implement
        case SubscribeFromGroupOnline(groupIds) ⇒
        // FIXME: implement
      }
  }

  override val requestStrategy = WatermarkRequestStrategy(10) // TODO: configurable

  // Publisher-related
  private[this] var protoMessageQueue = immutable.Queue.empty[ProtoMessage]

  def publisher: Receive = {
    case ub: UpdateBox ⇒ enqueueProtoMessage(ub)
    case Request(_)    ⇒ deliverBuf()
    case Cancel        ⇒ context.stop(self)
  }

  private def enqueueProtoMessage(message: ProtoMessage): Unit = {
    if (protoMessageQueue.isEmpty && totalDemand > 0) {
      onNext(message)
    } else {
      protoMessageQueue = protoMessageQueue.enqueue(message)
      deliverBuf()
    }
  }

  @tailrec final def deliverBuf(): Unit = {
    if (isActive && totalDemand > 0)
      protoMessageQueue.dequeueOption match {
        case Some((el, q)) ⇒
          protoMessageQueue = q
          onNext(el)
          deliverBuf()
        case None ⇒
      }
  }
}
