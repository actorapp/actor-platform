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

  override def handleSendMessage(outPeer: OutPeer, randomId: Long, message: MessageContent)(implicit clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] = {
    val authorizedAction = requireAuth.map { clientUserId =>
      val action = withOutPeer(clientUserId, outPeer) {
        // TODO: record social relation
        val dateTime = new DateTime
        val dateMillis = dateTime.getMillis

        outPeer.`type` match {
          case PeerType.Private =>
            val ownUpdate = UpdateMessage(
              peer = outPeer.asPeer,
              senderUserId = clientUserId,
              date = dateMillis,
              randomId = randomId,
              message = message
            )

            val outUpdate = UpdateMessage(
              peer = Peer(PeerType.Private, clientUserId),
              senderUserId = clientUserId,
              date = dateMillis,
              randomId = randomId,
              message = message
            )

            // TODO: write history messages

            for {
              _ <- broadcastClientUpdate(seqUpdManagerRegion, clientUserId, ownUpdate)
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

  override def handleSendEncryptedMessage(
    peer: im.actor.api.rpc.peers.OutPeer,
    randomId: Long,
    encryptedMessage: Array[Byte],
    keys: Vector[im.actor.api.rpc.messaging.EncryptedAesKey],
    ownKeys: Vector[im.actor.api.rpc.messaging.EncryptedAesKey]
  )(implicit clientData: im.actor.api.rpc.ClientData): Future[HandlerResult[misc.ResponseSeqDate]] = throw new NotImplementedError()
}
