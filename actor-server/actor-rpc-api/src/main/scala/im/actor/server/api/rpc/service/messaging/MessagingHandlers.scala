package im.actor.server.api.rpc.service.messaging

import akka.util.Timeout
import im.actor.api.rpc.FutureResultRpc._
import im.actor.api.rpc._
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.misc._
import im.actor.api.rpc.peers._
import im.actor.config.ActorConfig
import im.actor.server.sequence.SeqStateDate

import scala.concurrent._

private[messaging] trait MessagingHandlers extends PeersImplicits {
  this: MessagingServiceImpl ⇒

  override implicit val ec: ExecutionContext = actorSystem.dispatcher
  private implicit val timeout: Timeout = ActorConfig.defaultTimeout

  override def doHandleSendMessage(outPeer: ApiOutPeer, randomId: Long, message: ApiMessage, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] =
    authorized(clientData) { implicit client ⇒
      (for (
        SeqStateDate(seq, state, date) ← fromFuture(dialogExt.sendMessage(
          peer = outPeer.asPeer,
          senderUserId = client.userId,
          senderAuthSid = client.authSid,
          senderAuthId = Some(client.authId),
          randomId = randomId,
          message = message,
          accessHash = Some(outPeer.accessHash)
        ))
      ) yield ResponseSeqDate(seq, state.toByteArray, date)).run
    }
}
