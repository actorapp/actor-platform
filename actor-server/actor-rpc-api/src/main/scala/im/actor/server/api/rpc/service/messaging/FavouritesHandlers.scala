package im.actor.server.api.rpc.service.messaging

import im.actor.api.rpc.messaging.ResponseDialogsOrder
import im.actor.api.rpc.ClientData
import im.actor.api.rpc.peers.ApiOutPeer

import scala.concurrent.Future

trait FavouritesHandlers {
  this: MessagingServiceImpl ⇒

  override def jhandleFavouriteDialog(peer: ApiOutPeer, clientData: ClientData): Future[HandlerResult[ResponseDialogsOrder]] =
    throw new RuntimeException("Not implemented")

  override def jhandleUnfavouriteDialog(peer: ApiOutPeer, clientData: ClientData): Future[HandlerResult[ResponseDialogsOrder]] =
    throw new RuntimeException("Not implemented")

}
