package im.actor.server.api.rpc.service.weak

import akka.actor.ActorSystem
import im.actor.api.rpc._
import im.actor.api.rpc.misc.ResponseVoid
import im.actor.api.rpc.peers.{ ApiOutPeer, ApiPeer, ApiPeerType }
import im.actor.api.rpc.weak.{ ApiTypingType, UpdateTyping, WeakService }
import im.actor.server.db.DbExtension
import im.actor.server.persist
import im.actor.server.presences.PresenceExtension
import im.actor.server.sequence.WeakUpdatesExtension
import slick.driver.PostgresDriver.api._

import scala.concurrent.{ ExecutionContext, Future }

class WeakServiceImpl(implicit actorSystem: ActorSystem) extends WeakService {
  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  private val presenceExt = PresenceExtension(actorSystem)
  private val weakUpdatesExt = WeakUpdatesExtension(actorSystem)
  private val db = DbExtension(actorSystem).db

  override def jhandleTyping(peer: ApiOutPeer, typingType: ApiTypingType.ApiTypingType, clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      val action = peer.`type` match {
        case ApiPeerType.Private ⇒
          val update = UpdateTyping(ApiPeer(ApiPeerType.Private, client.userId), client.userId, typingType)
          val reduceKey = weakUpdatesExt.reduceKey(update.header, update.peer)

          weakUpdatesExt.broadcastUserWeakUpdate(peer.id, update, reduceKey = Some(reduceKey))
        case ApiPeerType.Group ⇒
          val update = UpdateTyping(ApiPeer(ApiPeerType.Group, peer.id), client.userId, typingType)
          val reduceKey = weakUpdatesExt.reduceKey(update.header, update.peer)

          for {
            otherUserIds ← persist.GroupUserRepo.findUserIds(peer.id) map (_.filterNot(_ == client.userId))
            _ ← DBIO.sequence(otherUserIds map (weakUpdatesExt.broadcastUserWeakUpdate(_, update, Some(reduceKey))))
          } yield ()
      }

      for (_ ← action) yield Ok(ResponseVoid)
    }

    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleSetOnline(isOnline: Boolean, timeout: Long, clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val authorizedAction = requireAuth(clientData).map { client ⇒

      if (isOnline) {
        presenceExt.presenceSetOnline(client.userId, client.authId, timeout)
      } else {
        presenceExt.presenceSetOffline(client.userId, client.authId, timeout)
      }

      DBIO.successful(Ok(ResponseVoid))
    }

    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleStopTyping(peer: ApiOutPeer, typingType: ApiTypingType.ApiTypingType, clientData: ClientData): Future[HandlerResult[ResponseVoid]] =
    Future.failed(new RuntimeException("Not implemented"))
}
