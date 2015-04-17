package im.actor.server.api.rpc.service.messaging

import im.actor.api.rpc._
import im.actor.server.models
import im.actor.server.persist

trait HistoryHandlers {
  self: MessagingServiceImpl =>
/*
  override def jhandleMessageReceived(peer: im.actor.api.rpc.peers.OutPeer, date: Long, clientData: im.actor.api.rpc.ClientData): scala.concurrent.Future[scalaz.\/[im.actor.api.rpc.RpcError,im.actor.api.rpc.misc.ResponseVoid]] = {
    val action = requireAuth(clientData).flatMap { client =>

    }

    db.run(toDBIOAction(action.transactionally))
  }*/
}
