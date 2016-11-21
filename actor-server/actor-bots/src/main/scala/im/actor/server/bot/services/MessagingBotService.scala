package im.actor.server.bot.services

import akka.actor.ActorSystem
import im.actor.api.rpc.PeersImplicits
import im.actor.bots.BotMessages.BotError
import im.actor.concurrent.FutureResult
import im.actor.server.bot.{ BotServiceBase, BotToApiConversions }
import im.actor.server.db.DbExtension
import im.actor.server.dialog.DialogExtension
import im.actor.server.messaging.MessageUpdating
import im.actor.server.model.{ Peer ⇒ ModelPeer }
import im.actor.server.persist.HistoryMessageRepo
import im.actor.server.sequence.SeqStateDate

private[bot] object MessagingBotErrors {
  val Forbidden = BotError(403, "FORBIDDEN")
}

private[bot] final class MessagingBotService(system: ActorSystem) extends BotServiceBase(system)
  with FutureResult[BotError]
  with BotToApiConversions
  with PeersImplicits
  with MessageUpdating {

  import MessagingBotErrors._
  import im.actor.bots.BotMessages._
  import system.dispatcher

  private lazy val dialogExt = DialogExtension(system)
  private lazy val db = DbExtension(system).db

  override val handlers: PartialFunction[RequestBody, WeakRequestHandler] = {
    case SendMessage(peer, randomId, message)          ⇒ sendMessage(peer, randomId, message).toWeak
    case UpdateMessageContent(peer, randomId, message) ⇒ updateMessage(peer, randomId, message).toWeak
  }

  private def sendMessage(peer: OutPeer, randomId: Long, message: MessageBody) = RequestHandler[SendMessage, SendMessage#Response](
    (botUserId: BotUserId, botAuthId: BotAuthId, botAuthSid: BotAuthSid) ⇒ {
      for {
        SeqStateDate(_, _, date) ← dialogExt.sendMessage(
          peer = peer,
          senderUserId = botUserId,
          senderAuthId = botAuthId,
          randomId = randomId,
          message = message,
          accessHash = peer.accessHash,
          isFat = false
        )
      } yield Right(MessageSent(date))
    }
  )

  // allow bot to update only it's messages. Bot won't not be able to modify user's messages
  private def updateMessage(peer: OutPeer, randomId: Long, updatedMessage: MessageBody) = RequestHandler[UpdateMessageContent, UpdateMessageContent#Response](
    (botUserId: BotUserId, botAuthId: BotAuthId, botAuthSid: BotAuthSid) ⇒ {
      val peerModel = toPeer(peer).asModel
      val botPeer = ModelPeer.privat(botUserId)
      (for {
        _ ← fromFutureBoolean(Forbidden)(db.run(HistoryMessageRepo.findBySender(botUserId, peerModel, randomId).headOption map (_.nonEmpty)))
        _ ← fromFuture(updateMessageContent(botUserId, 0L, peerModel, randomId, updatedMessage)(system))
      } yield MessageContentUpdated).value
    }
  )

}
