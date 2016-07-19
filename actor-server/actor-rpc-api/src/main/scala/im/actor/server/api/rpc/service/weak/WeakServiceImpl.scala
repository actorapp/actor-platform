package im.actor.server.api.rpc.service.weak

import akka.actor.ActorSystem
import akka.http.scaladsl.util.FastFuture
import im.actor.api.rpc._
import im.actor.api.rpc.misc.ResponseVoid
import im.actor.api.rpc.peers.{ ApiOutPeer, ApiPeer, ApiPeerType }
import im.actor.api.rpc.weak._
import im.actor.concurrent.FutureExt
import im.actor.server.db.DbExtension
import im.actor.server.group.{ CanSendMessageInfo, GroupExtension }
import im.actor.server.presences.PresenceExtension
import im.actor.server.sequence.WeakUpdatesExtension
import slick.driver.PostgresDriver.api._

import scala.concurrent.{ ExecutionContext, Future }

class WeakServiceImpl(implicit actorSystem: ActorSystem) extends WeakService {
  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  private val presenceExt = PresenceExtension(actorSystem)
  private val weakUpdatesExt = WeakUpdatesExtension(actorSystem)
  private val db = DbExtension(actorSystem).db

  private lazy val groupExt = GroupExtension(actorSystem)

  override def doHandleTyping(peer: ApiOutPeer, typingType: ApiTypingType.ApiTypingType, clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    authorized(clientData) { client ⇒
      peer.`type` match {
        case ApiPeerType.EncryptedPrivate ⇒
          val update = UpdateTyping(ApiPeer(ApiPeerType.EncryptedPrivate, client.userId), client.userId, typingType)
          val reduceKey = weakUpdatesExt.reduceKey(update.header, update.peer)

          weakUpdatesExt.broadcastUserWeakUpdate(peer.id, update, reduceKey = Some(reduceKey))
        case ApiPeerType.Private ⇒
          val update = UpdateTyping(ApiPeer(ApiPeerType.Private, client.userId), client.userId, typingType)
          val reduceKey = weakUpdatesExt.reduceKey(update.header, update.peer)

          weakUpdatesExt.broadcastUserWeakUpdate(peer.id, update, reduceKey = Some(reduceKey))
        case ApiPeerType.Group ⇒
          val update = UpdateTyping(ApiPeer(ApiPeerType.Group, peer.id), client.userId, typingType)
          val reduceKey = weakUpdatesExt.reduceKey(update.header, update.peer, client.userId)

          for {
            CanSendMessageInfo(_, isChannel, memberIds, _) ← groupExt.canSendMessage(peer.id, client.userId)
            _ ← if (isChannel)
              FastFuture.successful(())
            else
              FutureExt.ftraverse((memberIds - client.userId).toSeq)(weakUpdatesExt.broadcastUserWeakUpdate(_, update, Some(reduceKey)))
          } yield ()
      }

      FastFuture.successful(Ok(ResponseVoid))
    }
  }

  override def doHandleSetOnline(
    isOnline:       Boolean,
    timeout:        Long,
    deviceCategory: Option[ApiDeviceType.Value],
    deviceType:     Option[String],
    clientData:     ClientData
  ): Future[HandlerResult[ResponseVoid]] =
    authorized(clientData) { client ⇒

      if (isOnline) {
        presenceExt.presenceSetOnline(client.userId, client.authId, timeout)
      } else {
        presenceExt.presenceSetOffline(client.userId, client.authId, timeout)
      }

      FastFuture.successful(Ok(ResponseVoid))
    }

  override def doHandlePauseNotifications(timeout: Int, clientData: ClientData): Future[HandlerResult[ResponseVoid]] =
    Future.failed(new RuntimeException("Not implemented"))

  override def doHandleRestoreNotifications(clientData: ClientData): Future[HandlerResult[ResponseVoid]] =
    Future.failed(new RuntimeException("Not implemented"))

  // TODO: DRY
  override def doHandleStopTyping(peer: ApiOutPeer, typingType: ApiTypingType.ApiTypingType, clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    authorized(clientData) { client ⇒
      peer.`type` match {
        case ApiPeerType.EncryptedPrivate ⇒
          val update = UpdateTypingStop(ApiPeer(ApiPeerType.EncryptedPrivate, client.userId), client.userId, typingType)
          val reduceKey = weakUpdatesExt.reduceKey(update.header, update.peer)

          weakUpdatesExt.broadcastUserWeakUpdate(peer.id, update, reduceKey = Some(reduceKey))
        case ApiPeerType.Private ⇒
          val update = UpdateTypingStop(ApiPeer(ApiPeerType.Private, client.userId), client.userId, typingType)
          val reduceKey = weakUpdatesExt.reduceKey(update.header, update.peer)

          weakUpdatesExt.broadcastUserWeakUpdate(peer.id, update, reduceKey = Some(reduceKey))
        case ApiPeerType.Group ⇒
          val update = UpdateTypingStop(ApiPeer(ApiPeerType.Group, peer.id), client.userId, typingType)
          val reduceKey = weakUpdatesExt.reduceKey(update.header, update.peer)

          for {
            CanSendMessageInfo(_, isChannel, memberIds, _) ← groupExt.canSendMessage(peer.id, client.userId)
            _ ← if (isChannel)
              FastFuture.successful(())
            else
              FutureExt.ftraverse((memberIds - client.userId).toSeq)(weakUpdatesExt.broadcastUserWeakUpdate(_, update, Some(reduceKey)))
          } yield ()
      }

      FastFuture.successful(Ok(ResponseVoid))
    }
  }

}
