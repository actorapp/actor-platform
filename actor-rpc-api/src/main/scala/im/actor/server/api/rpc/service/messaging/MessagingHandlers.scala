package im.actor.server.api.rpc.service.messaging

import scala.concurrent._

import org.joda.time.DateTime
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._, messaging._, misc._, peers._, Implicits._

private[messaging] trait MessagingHandlers extends PeerHelpers {
  self: MessagingServiceImpl =>

  import im.actor.server.push.SeqUpdatesManager._

  override implicit val ec = actorSystem.dispatcher

  override def jhandleSendMessage(outPeer: OutPeer, randomId: Long, message: MessageContent, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client =>
      val action = withOutPeer(client.userId, outPeer) {
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

            // TODO: write history messages

            for {
              _ <- broadcastClientUpdate(seqUpdManagerRegion, ownUpdate)
              _ <- broadcastUserUpdate(seqUpdManagerRegion, outPeer.id, outUpdate)
              seqstate <- sendClientUpdate(seqUpdManagerRegion, UpdateMessageSent(outPeer.asPeer, randomId, dateMillis))
            } yield {
              Ok(ResponseSeqDate(seqstate._1, seqstate._2, dateMillis))
            }
        }
      }

      action.transactionally
    }

    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleSendEncryptedMessage(
    peer: im.actor.api.rpc.peers.OutPeer,
    randomId: Long,
    encryptedMessage: Array[Byte],
    keys: Vector[im.actor.api.rpc.messaging.EncryptedAesKey],
    ownKeys: Vector[im.actor.api.rpc.messaging.EncryptedAesKey],
    clientData: im.actor.api.rpc.ClientData
  ): Future[HandlerResult[misc.ResponseSeqDate]] = throw new NotImplementedError()
}
