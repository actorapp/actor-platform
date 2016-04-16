package im.actor.server.api.rpc.service.messaging

import im.actor.api.rpc.messaging.{ ResponseReactionsResponse, RequestMessageSetReaction, RequestMessageRemoveReaction }
import im.actor.api.rpc.peers.ApiOutPeer
import im.actor.api.rpc._
import im.actor.server.ApiConversions._

import scala.concurrent.Future

trait ReactionsHandlers {
  this: MessagingServiceImpl ⇒

  override def doHandleMessageSetReaction(peer: ApiOutPeer, randomId: Long, code: String, clientData: ClientData): Future[HandlerResult[RequestMessageSetReaction.Response]] = {
    authorized(clientData) { implicit client ⇒
      for {
        res ← dialogExt.setReaction(client.userId, client.authSid, peer.asModel, randomId, code)
      } yield Ok(ResponseReactionsResponse(
        res.getSeqstate.seq,
        res.getSeqstate.state.toByteArray,
        res.reactions
      ))
    }
  }

  override def doHandleMessageRemoveReaction(peer: ApiOutPeer, randomId: Long, code: String, clientData: ClientData): Future[HandlerResult[RequestMessageRemoveReaction.Response]] = {
    authorized(clientData) { implicit client ⇒
      for {
        res ← dialogExt.removeReaction(client.userId, client.authSid, peer.asModel, randomId, code)
      } yield Ok(ResponseReactionsResponse(
        res.getSeqstate.seq,
        res.getSeqstate.state.toByteArray,
        res.reactions
      ))
    }
  }
}
