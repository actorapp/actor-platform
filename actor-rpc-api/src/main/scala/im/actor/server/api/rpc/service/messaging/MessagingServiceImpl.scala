package im.actor.server.api.rpc.service.messaging

import scala.concurrent.Future

import akka.actor._
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._, messaging._
import im.actor.api.rpc.misc.ResponseVoid
import im.actor.api.rpc.peers.OutPeer
import im.actor.server.push.SeqUpdatesManagerRegion

class MessagingServiceImpl(implicit
                           val seqUpdManagerRegion: SeqUpdatesManagerRegion,
                           val db: Database,
                           val actorSystem: ActorSystem)
    extends MessagingService with MessagingHandlers with HistoryHandlers {
  override def jhandleEncryptedRead(peer: im.actor.api.rpc.peers.OutPeer,randomId: Long, clientData: im.actor.api.rpc.ClientData): scala.concurrent.Future[scalaz.\/[im.actor.api.rpc.RpcError,im.actor.api.rpc.misc.ResponseVoid]] = Future {
    throw new Exception("Not implemented")
  }

  override def jhandleEncryptedReceived(peer: im.actor.api.rpc.peers.OutPeer,randomId: Long, clientData: im.actor.api.rpc.ClientData): scala.concurrent.Future[scalaz.\/[im.actor.api.rpc.RpcError,im.actor.api.rpc.misc.ResponseVoid]] = Future {
    throw new Exception("Not implemented")
  }
}
