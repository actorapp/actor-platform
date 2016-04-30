package im.actor.server.api.rpc.service.messaging

import im.actor.api.rpc.messaging.ResponseDialogsOrder
import im.actor.api.rpc._
import im.actor.api.rpc.peers.ApiOutPeer
import im.actor.server.sequence.SeqState

import scala.concurrent.Future

trait FavouritesHandlers extends PeersImplicits {
  this: MessagingServiceImpl ⇒

  override def doHandleFavouriteDialog(peer: ApiOutPeer, clientData: ClientData): Future[HandlerResult[ResponseDialogsOrder]] =
    authorized(clientData) { client ⇒
      for {
        SeqState(seq, state) ← dialogExt.favourite(client.userId, peer.asModel)
        groups ← dialogExt.fetchApiGroupedDialogs(client.userId)
      } yield Ok(ResponseDialogsOrder(seq, state.toByteArray, groups))
    }

  override def doHandleUnfavouriteDialog(peer: ApiOutPeer, clientData: ClientData): Future[HandlerResult[ResponseDialogsOrder]] =
    authorized(clientData) { client ⇒
      for {
        SeqState(seq, state) ← dialogExt.unfavourite(client.userId, peer.asModel)
        groups ← dialogExt.fetchApiGroupedDialogs(client.userId)
      } yield Ok(ResponseDialogsOrder(seq, state.toByteArray, groups))
    }
}
