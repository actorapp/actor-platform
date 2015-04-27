package im.actor.server.api.rpc.service.messaging

import scala.concurrent._
import scala.concurrent.duration._

import akka.util.Timeout
import org.joda.time.DateTime
import slick.dbio
import slick.dbio.Effect.Read
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.misc._
import im.actor.api.rpc.peers._
import im.actor.server.api.util.{ ContactsUtils, HistoryUtils, PeerUtils, UserUtils }
import im.actor.server.push.SeqUpdatesManager
import im.actor.server.social.SocialManager
import im.actor.server.{ models, persist }

private[messaging] trait MessagingHandlers {
  self: MessagingServiceImpl ⇒

  import im.actor.api.rpc.Implicits._
  import HistoryUtils._
  import PeerUtils._
  import SeqUpdatesManager._
  import SocialManager._
  import UserUtils._
  import ContactsUtils._

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
              clientUser ← getClientUserUnsafe
              pushText ← getPushText(outUpdate.message, clientUser, outPeer.id)
              _ ← broadcastUserUpdate(outPeer.id, outUpdate, Some(pushText))
              _ ← DBIO.from(recordRelation(client.userId, outPeer.id))
              _ ← notifyClientUpdate(ownUpdate, None)
              seqstate ← persistAndPushUpdate(client.authId, update, None)
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
              _ ← writeHistoryMessage(models.Peer.privat(client.userId), models.Peer.group(outPeer.id), dateTime, randomId, message.header, message.toByteArray)
              _ ← broadcastGroupMessage(outPeer.id, outUpdate)
              seqstate ← persistAndPushUpdate(client.authId, update, None)
            } yield {
              Ok(ResponseSeqDate(seqstate._1, seqstate._2, dateMillis))
            }
        }
      }
    }

    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
  }

  private def broadcastGroupMessage(groupId: Int, update: UpdateMessage)(implicit clientData: AuthorizedClientData) = {
    val updateHeader = update.header
    val updateData = update.toByteArray
    val (updateUserIds, updateGroupIds) = updateRefs(update)

    for {
      userIds ← persist.GroupUser.findUserIds(groupId)
      clientUser ← getClientUserUnsafe
      seqstates ← DBIO.sequence(userIds.view.filterNot(_ == clientData.userId) map { userId ⇒
        for {
          pushText ← getPushText(update.message, clientUser, userId)
          seqstates ← broadcastUserUpdate(userId, updateHeader, updateData, updateUserIds, updateGroupIds, Some(pushText))
        } yield seqstates
      }) map (_.flatten)
      selfseqstates ← notifyClientUpdate(updateHeader, updateData, updateUserIds, updateGroupIds, None)
    } yield seqstates ++ selfseqstates
  }

  private def getPushText(message: Message, clientUser: models.User, outUser: Int): dbio.DBIOAction[String, NoStream, Read] = {
    message match {
      case TextMessage(text, _) ⇒
        for (localName ← getLocalNameOrDefault(outUser, clientUser))
          yield formatAuthored(localName, text)
      case dm: DocumentMessage ⇒
        getLocalNameOrDefault(outUser, clientUser) map { localName ⇒
          dm.ext match {
            case Some(_: DocumentExPhoto) ⇒
              formatAuthored(localName, "Photo")
            case Some(_: DocumentExVideo) ⇒
              formatAuthored(localName, "Video")
            case _ ⇒
              formatAuthored(localName, dm.name)
          }
        }
      case unsupported ⇒
        actorSystem.log.error("Unsupported message content {}", unsupported)
        DBIO.successful("")
    }
  }

  private def formatAuthored(authorName: String, message: String): String = s"${authorName}: ${message}"
}
