package im.actor.server.api.rpc.service.messaging

import scala.concurrent.Future

import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.messaging.{ UpdateMessageRead, UpdateMessageReadByMe, UpdateMessageReceived }
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
            _ <- broadcastUserUpdate(peer.id, update)
          } yield {
            Ok(ResponseVoid)
          }
        case PeerType.Group =>
          val update = UpdateMessageReceived(Peer(PeerType.Group, peer.id), date, receivedDate)

          for {
            // TODO: #perf avoid repeated extraction of group user ids (send updates inside markMessagesReceived?)
            otherGroupUserIds <- persist.GroupUser.findUserIds(peer.id).map(_.filterNot(_ == client.userId).toSet)
            otherAuthIds <- persist.AuthId.findIdByUserIds(otherGroupUserIds).map(_.toSet)
            _ <- markMessagesReceived(models.Peer.privat(client.userId), models.Peer.group(peer.id), new DateTime(date))
            _ <- persistAndPushUpdates(otherAuthIds, update)
          } yield {
            Ok(ResponseVoid)
          }
        case _ => throw new Exception("Not implemented")
      }
    }

    db.run(toDBIOAction(action map (_.transactionally)))
  }

  override def jhandleMessageRead(peer: OutPeer, date: Long, clientData: ClientData): Future[HandlerResult[ResponseVoid]] = {
    val action = requireAuth(clientData).map { implicit client =>
      val readDate = System.currentTimeMillis()

      peer.`type` match {
        case PeerType.Private =>
          val update = UpdateMessageRead(Peer(PeerType.Private, client.userId), date, readDate)
          val ownUpdate = UpdateMessageReadByMe(Peer(PeerType.Private, peer.id), date)

          for {
            _ <- markMessagesRead(models.Peer.privat(client.userId), models.Peer.privat(peer.id), new DateTime(date))
            _ <- broadcastUserUpdate(peer.id, update)
            _ <- broadcastClientUpdate(ownUpdate)
          } yield {
            Ok(ResponseVoid)
          }
        case PeerType.Group =>
          val groupPeer = Peer(PeerType.Group, peer.id)
          val update = UpdateMessageRead(groupPeer, date, readDate)
          val ownUpdate = UpdateMessageReadByMe(groupPeer, date)

          for {
          // TODO: #perf avoid repeated extraction of group user ids (send updates inside markMessagesReceived?)
            otherGroupUserIds <- persist.GroupUser.findUserIds(peer.id).map(_.filterNot(_ == client.userId).toSet)
            otherAuthIds <- persist.AuthId.findIdByUserIds(otherGroupUserIds).map(_.toSet)
            _ <- markMessagesRead(models.Peer.privat(client.userId), models.Peer.group(peer.id), new DateTime(date))
            _ <- persistAndPushUpdates(otherAuthIds, update)
            _ <- broadcastClientUpdate(ownUpdate)
          } yield {
            Ok(ResponseVoid)
          }
        case _ => throw new Exception("Not implemented")
      }
    }

    db.run(toDBIOAction(action map (_.transactionally)))
  }
}
