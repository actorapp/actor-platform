package im.actor.server.api.rpc.service.weak

import scala.concurrent.{ Future, ExecutionContext }

import akka.actor.{ ActorRef, ActorSystem }

import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.Implicits._
import im.actor.api.rpc.misc.ResponseVoid
import im.actor.api.rpc.peers.{ PeerType, Peer, OutPeer }
import im.actor.api.rpc.weak.{ UpdateTyping, WeakService }
import im.actor.server.presences.{ PresenceManagerRegion, PresenceManager }
import im.actor.server.push.{ WeakUpdatesManagerRegion, WeakUpdatesManager }

class WeakServiceImpl(implicit
                      weakUpdManagerRegion: WeakUpdatesManagerRegion,
                      presenceManagerRegion: PresenceManagerRegion,
                      db: Database,
                      actorSystem: ActorSystem) extends WeakService {
  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  override def jhandleTyping(peer: OutPeer, typingType: Int, clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client =>
      val update = peer.`type` match {
        case PeerType.Private =>
          UpdateTyping(Peer(PeerType.Private, client.userId), client.userId, typingType)
        case PeerType.Group =>
          UpdateTyping(Peer(PeerType.Group, peer.id), client.userId, typingType)
      }

      for (_ <- WeakUpdatesManager.broadcastUserWeakUpdate(peer.id, update)) yield {
        Ok(ResponseVoid)
      }
    }

    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleSetOnline(isOnline: Boolean, timeout: Long, clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val authorizedAction = requireAuth(clientData).map { client =>

      if (isOnline) {
        PresenceManager.presenceSetOnline(client.userId, timeout)
      } else {
        PresenceManager.presenceSetOffline(client.userId, timeout)
      }

      DBIO.successful(Ok(ResponseVoid))
    }

    db.run(toDBIOAction(authorizedAction))
  }
}
