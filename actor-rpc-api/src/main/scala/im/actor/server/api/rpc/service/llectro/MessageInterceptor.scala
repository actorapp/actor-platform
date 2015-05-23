package im.actor.server.api.rpc.service.llectro

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import akka.actor.{ Actor, ActorLogging }
import akka.contrib.pattern.{ DistributedPubSubExtension, DistributedPubSubMediator }

import im.actor.api.rpc.peers.{ PeerType, Peer }
import im.actor.server.api.rpc.service.messaging.MessagingService
import im.actor.server.persist

object MessageInterceptor {
  private case object FetchUserIds
  private case class SubscribeUsers(ids: Seq[Int])
  private[llectro] case class Resubscribe(peer: Peer)
}

class MessageInterceptor extends Actor with ActorLogging {
  import DistributedPubSubMediator._

  import MessageInterceptor._

  implicit val ec: ExecutionContext = context.dispatcher

  val mediator = DistributedPubSubExtension(context.system).mediator
  val scheduledFetch = context.system.scheduler.schedule(0.seconds, 1.minute, self, FetchUserIds)

  var subscribedUserIds = Set.empty[Int]

  override def postStop(): Unit = {
    super.postStop()
    scheduledFetch.cancel()
  }

  def receive = {
    case FetchUserIds ⇒
      fetchUserIds()
    case SubscribeUsers(ids) ⇒
      val newIds = ids.toSet.diff(subscribedUserIds)

      newIds foreach { id ⇒
        log.debug("Subscribing to {}", id)
        val interceptor = context.actorOf(PrivatePeerInterceptor.props(id), s"ilectro/interceptor/private/${id}")
        mediator ! Subscribe(MessagingService.messagesTopic(Peer(PeerType.Private, id)), None, interceptor)
      }

      subscribedUserIds ++= newIds
    case Resubscribe(peer) ⇒
      log.debug("Resubscribe {}", peer)
      mediator ! Subscribe(MessagingService.messagesTopic(peer), None, sender())
    case _ ⇒
  }

  private def fetchUserIds(): Unit = {
    for (userIds ← persist.ilectro.ILectroUser.findIds) yield {
      self ! SubscribeUsers(userIds)
    }
  }
}
