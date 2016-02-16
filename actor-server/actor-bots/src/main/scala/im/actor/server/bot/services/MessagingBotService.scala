package im.actor.server.bot.services

import akka.actor.ActorSystem
import im.actor.api.rpc.PeersImplicits
import im.actor.api.rpc.messaging.{ ApiMessage, UpdateMessageContentChanged }
import im.actor.bots.BotMessages.BotError
import im.actor.concurrent.FutureResultCats
import im.actor.server.bot.{ BotServiceBase, BotToApiConversions }
import im.actor.server.db.DbExtension
import im.actor.server.dialog.DialogExtension
import im.actor.server.group.GroupExtension
import im.actor.server.model.{ Peer ⇒ ModelPeer, PeerType }
import im.actor.server.persist.HistoryMessageRepo
import im.actor.server.sequence.SeqStateDate

import scala.concurrent.Future

private[bot] object MessagingBotErrors {
  val Forbidden = BotError(403, "FORBIDDEN")
}

private[bot] final class MessagingBotService(system: ActorSystem) extends BotServiceBase(system)
  with FutureResultCats[BotError]
  with BotToApiConversions
  with PeersImplicits {

  import MessagingBotErrors._
  import im.actor.bots.BotMessages._
  import system.dispatcher

  private lazy val dialogExt = DialogExtension(system)
  private lazy val db = DbExtension(system).db

  override val handlers: PartialFunction[RequestBody, WeakRequestHandler] = {
    case SendMessage(peer, randomId, message)          ⇒ sendMessage(peer, randomId, message).toWeak
    case UpdateMessageContent(peer, randomId, message) ⇒ updateMessageContent(peer, randomId, message).toWeak
  }

  private def sendMessage(peer: OutPeer, randomId: Long, message: MessageBody) = RequestHandler[SendMessage, SendMessage#Response](
    (botUserId: BotUserId, botAuthId: BotAuthId, botAuthSid: BotAuthSid) ⇒ {
      for {
        SeqStateDate(_, _, date) ← dialogExt.sendMessage(
          peer = peer,
          senderUserId = botUserId,
          senderAuthSid = botAuthSid,
          senderAuthId = Some(botAuthId),
          randomId = randomId,
          message = message,
          accessHash = Some(peer.accessHash),
          isFat = false
        )
      } yield Right(MessageSent(date))
    }
  )

  // allow bot to update only it's messages. Bot won't not be able to modify user's messages
  private def updateMessageContent(peer: OutPeer, randomId: Long, updatedMessage: MessageBody) = RequestHandler[UpdateMessageContent, UpdateMessageContent#Response](
    (botUserId: BotUserId, botAuthId: BotAuthId, botAuthSid: BotAuthSid) ⇒ {
      val peerModel = toPeer(peer).asModel
      val botPeer = ModelPeer.privat(botUserId)
      (for {
        _ ← fromFutureBoolean(Forbidden)(db.run(HistoryMessageRepo.findBySender(botUserId, peerModel, randomId).headOption map (_.nonEmpty)))
        _ ← fromFuture(peer match {
          case UserOutPeer(userId, _)   ⇒ updateContentPrivate(peerModel.id, botPeer, randomId, updatedMessage)
          case GroupOutPeer(groupId, _) ⇒ updateContentGroup(botUserId, peerModel, randomId, updatedMessage)
        })
      } yield MessageContentUpdated).value
    }
  )

  private def updateContentPrivate(userId: Int, botPeer: ModelPeer, randomId: Long, updatedMessage: ApiMessage): Future[Unit] = {
    val upd = UpdateMessageContentChanged(botPeer.asStruct, randomId, updatedMessage)
    for {
      _ ← userExt.broadcastUserUpdate(
        userId = userId,
        update = upd,
        pushText = None,
        isFat = false,
        reduceKey = None,
        deliveryId = Some(s"msgcontent_$randomId")
      )
      _ ← db.run(HistoryMessageRepo.updateContentAll(
        userIds = Set(userId, botPeer.id),
        randomId = randomId,
        peerType = PeerType.Private,
        peerIds = Set(userId, botPeer.id),
        messageContentHeader = updatedMessage.header,
        messageContentData = updatedMessage.toByteArray
      ))
    } yield ()
  }

  private def updateContentGroup(botUserId: Int, groupPeer: ModelPeer, randomId: Long, updatedMessage: ApiMessage): Future[Unit] = {
    //no need to send update to bot itself
    val upd = UpdateMessageContentChanged(groupPeer.asStruct, randomId, updatedMessage)
    for {
      (memberIds, _, _) ← GroupExtension(system).getMemberIds(groupPeer.id)
      membersSet = memberIds.toSet
      _ ← userExt.broadcastUsersUpdate(
        userIds = membersSet,
        update = upd,
        pushText = None,
        isFat = false,
        deliveryId = Some(s"msgcontent_$randomId")
      )
      _ ← db.run(HistoryMessageRepo.updateContentAll(
        userIds = membersSet + botUserId,
        randomId = randomId,
        peerType = PeerType.Group,
        peerIds = Set(groupPeer.id),
        messageContentHeader = updatedMessage.header,
        messageContentData = updatedMessage.toByteArray
      ))
    } yield ()
  }

}