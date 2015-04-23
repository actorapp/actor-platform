package im.actor.server.api.rpc.service.messaging

import scala.concurrent._
import scala.concurrent.duration._

import akka.util.Timeout
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.misc._
import im.actor.api.rpc.peers._
import im.actor.server.{ models, persist }

private[messaging] trait MessagingHandlers {
  self: MessagingServiceImpl ⇒

  import im.actor.api.rpc.Implicits._
  import im.actor.server.api.util.HistoryUtils._
  import im.actor.server.api.util.PeerUtils._
  import im.actor.server.push.SeqUpdatesManager._
  import im.actor.server.social.SocialManager._

  override implicit val ec = actorSystem.dispatcher

  implicit val timeout = Timeout(5.seconds) // TODO: configurable

  override def jhandleSendMessage(outPeer: OutPeer, randomId: Long, message: Message, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      withOutPeer(client.userId, outPeer) {
        val dateTime = new DateTime
        val dateMillis = dateTime.getMillis

        outPeer.`type` match {
          case PeerType.Private ⇒
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
              _ ← writeHistoryMessage(models.Peer.privat(client.userId), models.Peer.privat(outPeer.id), dateTime, randomId, message.header, message.toByteArray)
              _ ← broadcastUserUpdate(outPeer.id, outUpdate)
              _ ← DBIO.from(recordRelation(client.userId, outPeer.id)) // TODO: configurable
              _ ← notifyClientUpdate(ownUpdate)
              seqstate ← persistAndPushUpdate(client.authId, update)
            } yield {
              Ok(ResponseSeqDate(seqstate._1, seqstate._2, dateMillis))
            }
          case PeerType.Group ⇒
            val outUpdate = UpdateMessage(
              peer = Peer(PeerType.Group, outPeer.id),
              senderUserId = client.userId,
              date = dateMillis,
              randomId = randomId,
              message = message
            )

            val update = UpdateMessageSent(outPeer.asPeer, randomId, dateMillis)

            for {
              userIds ← persist.GroupUser.findUserIds(outPeer.id)
              otherAuthIds ← persist.AuthId.findIdByUserIds(userIds.toSet).map(_.filterNot(_ == client.authId))
              _ ← writeHistoryMessage(models.Peer.privat(client.userId), models.Peer.group(outPeer.id), dateTime, randomId, message.header, message.toByteArray)
              _ ← persistAndPushUpdates(otherAuthIds.toSet, outUpdate)
              seqstate ← persistAndPushUpdate(client.authId, update)
            } yield {
              Ok(ResponseSeqDate(seqstate._1, seqstate._2, dateMillis))
            }
        }
      }
    }

    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
  }
}
