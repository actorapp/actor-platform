package im.actor.server.api.rpc.service.messaging

import scala.concurrent._

import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.Implicits._
import im.actor.api.rpc._
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.misc._
import im.actor.api.rpc.peers._
import im.actor.server.models
import im.actor.server.persist

private[messaging] trait MessagingHandlers {
  self: MessagingServiceImpl =>

  import im.actor.server.api.util.PeerUtils._
  import im.actor.server.api.util.HistoryUtils._
  import im.actor.server.push.SeqUpdatesManager._

  override implicit val ec = actorSystem.dispatcher

  override def jhandleSendMessage(outPeer: OutPeer, randomId: Long, message: MessageContent, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client =>
      withOutPeer(client.userId, outPeer) {
        // TODO: record social relation
        val dateTime = new DateTime
        val dateMillis = dateTime.getMillis

        outPeer.`type` match {
          case PeerType.Private =>
            val ownUpdate = UpdateMessage(
              peer = outPeer.asPeer,
              senderUserId = client.userId,
              date = dateMillis,
              randomId = randomId,
              message = message
            )

            val outUpdate = UpdateMessage(
              peer = Peer(PeerType.Private, client.userId),
              senderUserId = client.userId,
              date = dateMillis,
              randomId = randomId,
              message = message
            )

            val update = UpdateMessageSent(outPeer.asPeer, randomId, dateMillis)

            for {
              _ <- writeHistoryMessage(models.Peer.privat(client.userId), models.Peer.privat(outPeer.id), dateTime, randomId, message.`type`, message.toByteArray)
              _ <- broadcastUserUpdate(seqUpdManagerRegion, outPeer.id, outUpdate)
              _ <- notifyClientUpdate(seqUpdManagerRegion, ownUpdate)
              seqstate <- persistAndPushUpdate(seqUpdManagerRegion, client.authId, update)
            } yield {
              Ok(ResponseSeqDate(seqstate._1, seqstate._2, dateMillis))
            }
          case PeerType.Group =>
            val outUpdate = UpdateMessage(
              peer = Peer(PeerType.Group, outPeer.id),
              senderUserId = client.userId,
              date = dateMillis,
              randomId = randomId,
              message = message)

            val update = UpdateMessageSent(outPeer.asPeer, randomId, dateMillis)

            for {
              userIds <- persist.GroupUser.findUserIds(outPeer.id)
              otherAuthIds <- persist.AuthId.findIdByUserIds(userIds.toSet).map(_.filterNot(_ == client.authId))
              _ <- writeHistoryMessage(models.Peer.privat(client.userId), models.Peer.group(outPeer.id), dateTime, randomId, message.`type`, message.toByteArray)
              _ <- persistAndPushUpdates(seqUpdManagerRegion, otherAuthIds.toSet, outUpdate)
              seqstate <- persistAndPushUpdate(seqUpdManagerRegion, client.authId, update)
            } yield {
              Ok(ResponseSeqDate(seqstate._1, seqstate._2, dateMillis))
            }
        }
      }
    }

    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
  }

  override def jhandleSendEncryptedMessage(peer: im.actor.api.rpc.peers.OutPeer,
                                           randomId: Long,
                                           encryptedMessage: Array[Byte],
                                           keys: Vector[im.actor.api.rpc.messaging.EncryptedAesKey],
                                           ownKeys: Vector[im.actor.api.rpc.messaging.EncryptedAesKey],
                                           clientData: im.actor.api.rpc.ClientData): Future[HandlerResult[misc.ResponseSeqDate]] = throw new NotImplementedError()
}
