package im.actor.server.api.rpc.service.ilectro.interceptors

import scala.concurrent.ExecutionContext

import akka.actor.{ Actor, ActorLogging, ActorSystem }

import im.actor.api.rpc.peers.Peer
import im.actor.api.rpc.peers.PeerType._

object PeerInterceptor {
  private[ilectro] case object Resubscribe

  private[ilectro] def interceptorGroupId(peer: Peer): String = {
    peer match {
      case Peer(Group, id)   ⇒ s"group-$id"
      case Peer(Private, id) ⇒ s"private-$id"
    }
  }
}

trait PeerInterceptor extends Actor with ActorLogging {

  protected[this] implicit val system: ActorSystem = context.system
  protected[this] implicit val ec: ExecutionContext = system.dispatcher

}