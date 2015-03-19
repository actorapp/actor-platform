package im.actor.server.api.rpc.service.messaging

import akka.actor._
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._, messaging._

class MessagingServiceImpl(
  val seqUpdManagerRegion: ActorRef
)(implicit val db: Database, val actorSystem: ActorSystem)
    extends MessagingService with MessagingHandlers {
  override def handleClearChat(peer: im.actor.api.rpc.peers.OutPeer)(implicit clientData: im.actor.api.rpc.ClientData): scala.concurrent.Future[scalaz.\/[im.actor.api.rpc.RpcError,im.actor.api.rpc.misc.ResponseSeq]] = throw new NotImplementedError()

  override def handleDeleteChat(peer: im.actor.api.rpc.peers.OutPeer)(implicit clientData: im.actor.api.rpc.ClientData): scala.concurrent.Future[scalaz.\/[im.actor.api.rpc.RpcError,im.actor.api.rpc.misc.ResponseSeq]] = throw new NotImplementedError()

  override def handleDeleteMessage(peer: im.actor.api.rpc.peers.OutPeer,rids: scala.collection.immutable.Vector[Long])(implicit clientData: im.actor.api.rpc.ClientData): scala.concurrent.Future[scalaz.\/[im.actor.api.rpc.RpcError,im.actor.api.rpc.misc.ResponseVoid]] = throw new NotImplementedError()

  override def handleEncryptedRead(peer: im.actor.api.rpc.peers.OutPeer,randomId: Long)(implicit clientData: im.actor.api.rpc.ClientData): scala.concurrent.Future[scalaz.\/[im.actor.api.rpc.RpcError,im.actor.api.rpc.misc.ResponseVoid]] = throw new NotImplementedError()

  override def handleEncryptedReceived(peer: im.actor.api.rpc.peers.OutPeer,randomId: Long)(implicit clientData: im.actor.api.rpc.ClientData): scala.concurrent.Future[scalaz.\/[im.actor.api.rpc.RpcError,im.actor.api.rpc.misc.ResponseVoid]] = throw new NotImplementedError()

  override def handleMessageRead(peer: im.actor.api.rpc.peers.OutPeer,date: Long)(implicit clientData: im.actor.api.rpc.ClientData): scala.concurrent.Future[scalaz.\/[im.actor.api.rpc.RpcError,im.actor.api.rpc.misc.ResponseVoid]] = throw new NotImplementedError()

  override def handleMessageReceived(peer: im.actor.api.rpc.peers.OutPeer,date: Long)(implicit clientData: im.actor.api.rpc.ClientData): scala.concurrent.Future[scalaz.\/[im.actor.api.rpc.RpcError,im.actor.api.rpc.misc.ResponseVoid]] = throw new NotImplementedError()
}
