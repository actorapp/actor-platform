package im.actor.server.api.rpc.service.messaging

import scala.concurrent.Future

import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.messaging.{ UpdateMessageReadByMe, UpdateMessageReceived }
import im.actor.api.rpc.misc.ResponseVoid
import im.actor.api.rpc.peers.{ OutPeer, Peer, PeerType }
import im.actor.server.api.util.HistoryUtils
import im.actor.server.{ models, persist }

trait HistoryHandlers {
  self: MessagingServiceImpl =>

  import HistoryUtils._
  import im.actor.server.push.SeqUpdatesManager._

  override def jhandleMessageReceived(peer: OutPeer, date: Long, clientData: im.actor.api.rpc.ClientData): Future[HandlerResult[ResponseVoid]] = {
    val action = requireAuth(clientData).map { client =>
      val receivedDate = System.currentTimeMillis()

      peer.`type` match {
        case PeerType.Private =>
          val update = UpdateMessageReceived(Peer(PeerType.Private, client.userId), date, receivedDate)

          for {
            _ <- markMessagesReceived(models.Peer.privat(client.userId), models.Peer.privat(peer.id), new DateTime(date))
            _ <- broadcastUserUpdate(seqUpdManagerRegion, peer.id, update)
          } yield {
            Ok(ResponseVoid)
          }
        case PeerType.Group =>
          throw new NotImplementedError()
        case _ => throw new NotImplementedError()
      }
    }

    db.run(toDBIOAction(action map (_.transactionally)))
  }
}
