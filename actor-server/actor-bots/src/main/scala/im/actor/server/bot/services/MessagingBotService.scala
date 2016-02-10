package im.actor.server.bot.services

import akka.actor.ActorSystem
import im.actor.server.bot.{ BotToApiConversions, BotServiceBase }
import im.actor.server.dialog.DialogExtension
import im.actor.server.sequence.SeqStateDate

private[bot] final class MessagingBotService(system: ActorSystem) extends BotServiceBase(system) with BotToApiConversions {

  import im.actor.bots.BotMessages._
  import system.dispatcher

  private lazy val dialogExt = DialogExtension(system)

  override val handlers: PartialFunction[RequestBody, WeakRequestHandler] = {
    case SendMessage(peer, randomId, message) ⇒ sendMessage(peer, randomId, message).toWeak
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
}