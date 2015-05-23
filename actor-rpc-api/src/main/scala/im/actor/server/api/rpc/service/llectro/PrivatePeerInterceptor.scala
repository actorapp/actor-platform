package im.actor.server.api.rpc.service.llectro

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import akka.actor.{ Actor, ActorLogging, Props }
import akka.contrib.pattern.DistributedPubSubMediator

import im.actor.api.rpc.peers.{ PeerType, Peer }
import im.actor.server.api.rpc.service.messaging.Events

object PrivatePeerInterceptor {
  def props(userId: Int) = Props(classOf[PrivatePeerInterceptor], userId)
}

class PrivatePeerInterceptor(userId: Int) extends Actor with ActorLogging {
  import DistributedPubSubMediator._

  implicit val ec: ExecutionContext = context.dispatcher

  var countdown: Int = 10

  val scheduledResubscribe =
    context.system.scheduler.scheduleOnce(
      30.seconds, context.parent, MessageInterceptor.Resubscribe(Peer(PeerType.Private, userId))
    )

  def receive = {
    case ack: SubscribeAck ⇒
      scheduledResubscribe.cancel()
    case Events.PeerMessage(fromPeer, toPeer, randomId, message) ⇒
      countdown -= 1
      if (countdown == 0) {
        insertAds()
      }
  }

  private def insertAds(): Unit = {

  }
}