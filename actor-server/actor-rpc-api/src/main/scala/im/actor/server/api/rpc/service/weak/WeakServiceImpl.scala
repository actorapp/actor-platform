package im.actor.server.api.rpc.service.weak

import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.ActorSystem
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.misc.ResponseVoid
import im.actor.api.rpc.peers.{ OutPeer, Peer, PeerType }
import im.actor.api.rpc.weak.{ TypingType, UpdateTyping, WeakService }
import im.actor.server.persist
import im.actor.server.presences.{ PresenceManager, PresenceManagerRegion }
import im.actor.server.sequence.{ WeakUpdatesManager, WeakUpdatesManagerRegion }

class WeakServiceImpl(implicit
  weakUpdManagerRegion: WeakUpdatesManagerRegion,
                      presenceManagerRegion: PresenceManagerRegion,
                      db:                    Database,
                      actorSystem:           ActorSystem) extends WeakService {
  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  override def jhandleTyping(peer: OutPeer, typingType: TypingType.TypingType, clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      val action = peer.`type` match {
        case PeerType.Private ⇒
          val update = UpdateTyping(Peer(PeerType.Private, client.userId), client.userId, typingType)

          WeakUpdatesManager.broadcastUserWeakUpdate(peer.id, update)
        case PeerType.Group ⇒
          val update = UpdateTyping(Peer(PeerType.Group, peer.id), client.userId, typingType)

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
