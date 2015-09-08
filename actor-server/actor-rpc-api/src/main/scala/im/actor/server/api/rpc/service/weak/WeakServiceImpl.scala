package im.actor.server.api.rpc.service.weak

import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.ActorSystem
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.misc.ResponseVoid
import im.actor.api.rpc.peers.{ ApiOutPeer, ApiPeer, ApiPeerType }
import im.actor.api.rpc.weak.{ ApiTypingType, UpdateTyping, WeakService }
import im.actor.server.persist
import im.actor.server.presences.{ PresenceManager, PresenceManagerRegion }
import im.actor.server.sequence.{ WeakUpdatesManager, WeakUpdatesManagerRegion }

class WeakServiceImpl(implicit
  weakUpdManagerRegion: WeakUpdatesManagerRegion,
                      presenceManagerRegion: PresenceManagerRegion,
                      db:                    Database,
                      actorSystem:           ActorSystem) extends WeakService {
  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  override def jhandleTyping(peer: ApiOutPeer, typingType: ApiTypingType.ApiTypingType, clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      val action = peer.`type` match {
        case ApiPeerType.Private ⇒
          val update = UpdateTyping(ApiPeer(ApiPeerType.Private, client.userId), client.userId, typingType)

          WeakUpdatesManager.broadcastUserWeakUpdate(peer.id, update)
        case ApiPeerType.Group ⇒
          val update = UpdateTyping(ApiPeer(ApiPeerType.Group, peer.id), client.userId, typingType)

          for {
            otherUserIds ← persist.GroupUser.findUserIds(peer.id) map (_.filterNot(_ == client.userId))
            _ ← DBIO.sequence(otherUserIds map (WeakUpdatesManager.broadcastUserWeakUpdate(_, update)))
          } yield ()
      }

      for (_ ← action) yield Ok(ResponseVoid)
    }

    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleSetOnline(isOnline: Boolean, timeout: Long, clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val authorizedAction = requireAuth(clientData).map { client ⇒

      if (isOnline) {
        PresenceManager.presenceSetOnline(client.userId, client.authId, timeout)
      } else {
        PresenceManager.presenceSetOffline(client.userId, client.authId, timeout)
      }

      DBIO.successful(Ok(ResponseVoid))
    }

    db.run(toDBIOAction(authorizedAction))
  }
}
