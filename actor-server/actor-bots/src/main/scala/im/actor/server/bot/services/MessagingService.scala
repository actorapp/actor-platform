package im.actor.server.bot.services

import akka.actor.ActorSystem
import im.actor.api.rpc.messaging.ApiTextMessage
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.server.bot.BotService
import im.actor.server.dialog.DialogExtension
import im.actor.server.sequence.SeqStateDate

final class MessagingService(system: ActorSystem) extends BotService {

  import BotService._
  import im.actor.bots.BotMessages._
  import system.dispatcher

  private lazy val dialogExt = DialogExtension(system)

  override val handlers: PartialFunction[RequestBody, WeakRequestHandler] = {
    case SendTextMessage(peer, randomId, message) ⇒ sendTextMessage(peer, randomId, message).toWeak
  }

  private def sendTextMessage(peer: OutPeer, randomId: Long, message: String) = RequestHandler[SendTextMessage, SendTextMessage#Response](
    (botUserId: BotUserId, botAuthId: BotAuthId) ⇒ {
      // FIXME: check access hash
      for {
        SeqStateDate(_, _, date) ← dialogExt.sendMessage(
          peer = ApiPeer(ApiPeerType(peer.`type`), peer.id),
          senderUserId = botUserId,
          senderAuthId = 0L,
          randomId = randomId,
          message = ApiTextMessage(message, Vector.empty, None),
          isFat = false
        )
      } yield Right(MessageSent(date))
    }
  )
}