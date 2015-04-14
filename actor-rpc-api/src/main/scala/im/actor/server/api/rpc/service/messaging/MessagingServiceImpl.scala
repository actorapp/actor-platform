package im.actor.server.api.rpc.service.messaging

import akka.actor._
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._, messaging._

class MessagingServiceImpl(
  val seqUpdManagerRegion: ActorRef
)(implicit val db: Database, val actorSystem: ActorSystem)
    extends MessagingService with MessagingHandlers with HistoryHandlers {
  override def jhandleClearChat(peer: im.actor.api.rpc.peers.OutPeer, clientData: im.actor.api.rpc.ClientData): scala.concurrent.Future[scalaz.\/[im.actor.api.rpc.RpcError,im.actor.api.rpc.misc.ResponseSeq]] = throw new NotImplementedError()

  override def jhandleDeleteChat(peer: im.actor.api.rpc.peers.OutPeer, clientData: im.actor.api.rpc.ClientData): scala.concurrent.Future[scalaz.\/[im.actor.api.rpc.RpcError,im.actor.api.rpc.misc.ResponseSeq]] = throw new NotImplementedError()

  override def jhandleDeleteMessage(peer: im.actor.api.rpc.peers.OutPeer,rids: scala.collection.immutable.Vector[Long], clientData: im.actor.api.rpc.ClientData): scala.concurrent.Future[scalaz.\/[im.actor.api.rpc.RpcError,im.actor.api.rpc.misc.ResponseVoid]] = throw new NotImplementedError()

  override def jhandleEncryptedRead(peer: im.actor.api.rpc.peers.OutPeer,randomId: Long, clientData: im.actor.api.rpc.ClientData): scala.concurrent.Future[scalaz.\/[im.actor.api.rpc.RpcError,im.actor.api.rpc.misc.ResponseVoid]] = throw new NotImplementedError()

  override def jhandleEncryptedReceived(peer: im.actor.api.rpc.peers.OutPeer,randomId: Long, clientData: im.actor.api.rpc.ClientData): scala.concurrent.Future[scalaz.\/[im.actor.api.rpc.RpcError,im.actor.api.rpc.misc.ResponseVoid]] = throw new NotImplementedError()

  override def jhandleMessageRead(peer: im.actor.api.rpc.peers.OutPeer,date: Long, clientData: im.actor.api.rpc.ClientData): scala.concurrent.Future[scalaz.\/[im.actor.api.rpc.RpcError,im.actor.api.rpc.misc.ResponseVoid]] = throw new NotImplementedError()

  override def jhandleMessageReceived(peer: im.actor.api.rpc.peers.OutPeer,date: Long, clientData: im.actor.api.rpc.ClientData): scala.concurrent.Future[scalaz.\/[im.actor.api.rpc.RpcError,im.actor.api.rpc.misc.ResponseVoid]] = throw new NotImplementedError()
}
