package im.actor.server.bot.services

import akka.actor.ActorSystem
import im.actor.api.rpc.messaging.{ ApiJsonMessage, ApiTextMessage }
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.server.bot.BotServiceBase
import im.actor.server.dialog.DialogExtension
import im.actor.server.sequence.SeqStateDate

final class MessagingService(system: ActorSystem) extends BotServiceBase {

  import im.actor.bots.BotMessages._
  import system.dispatcher

  private lazy val dialogExt = DialogExtension(system)

  override val handlers: PartialFunction[RequestBody, WeakRequestHandler] = {
    case SendMessage(peer, randomId, message) ⇒ sendMessage(peer, randomId, message).toWeak
  }

  private def sendMessage(peer: OutPeer, randomId: Long, message: MessageBody) = RequestHandler[SendMessage, SendMessage#Response](
    (botUserId: BotUserId, botAuthId: BotAuthId) ⇒ {
      // FIXME: check access hash

      val apiMessage = message match {
        case TextMessage(text)    ⇒ ApiTextMessage(text, Vector.empty, None)
        case JsonMessage(rawJson) ⇒ ApiJsonMessage(rawJson)
      }

      for {
        SeqStateDate(_, _, date) ← dialogExt.sendMessage(
          peer = ApiPeer(ApiPeerType(peer.`type`), peer.id),
          senderUserId = botUserId,
          senderAuthId = 0L,
          randomId = randomId,
          message = apiMessage,
          isFat = false
        )
      } yield Right(MessageSent(date))
    }
  )
}