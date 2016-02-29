package im.actor.server.api.rpc.service.messaging

import akka.http.scaladsl.util.FastFuture
import akka.util.Timeout
import im.actor.api.rpc._
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.misc._
import im.actor.api.rpc.peers._
import im.actor.config.ActorConfig

import scala.concurrent._

private[messaging] trait MessagingHandlers extends PeersImplicits {
  this: MessagingServiceImpl ⇒

  import FutureResultRpc._

  override implicit val ec: ExecutionContext = actorSystem.dispatcher
  private implicit val timeout: Timeout = ActorConfig.defaultTimeout

  override def doHandleSendMessage(outPeer: ApiOutPeer, randomId: Long, message: ApiMessage, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] =
    authorized(clientData) { implicit client ⇒
      (for (
        s ← fromFuture(dialogExt.sendMessage(
          peer = outPeer.asPeer,
          senderUserId = client.userId,
          senderAuthSid = client.authSid,
          senderAuthId = Some(client.authId),
          randomId = randomId,
          message = message,
          accessHash = Some(outPeer.accessHash)
        ))
      ) yield ResponseSeqDate(s.seq, s.state.toByteArray, s.date)).value
    }

  override def doHandleUpdateMessage(peer: ApiOutPeer, randomId: Long, updatedMessage: ApiMessage, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] =
    FastFuture.failed(new RuntimeException("Not implemented"))
}
