package im.actor.server.bot.services

import akka.actor.ActorSystem
import akka.util.Timeout
import im.actor.bots.BotMessages
import im.actor.concurrent.FutureResultCats
import im.actor.config.ActorConfig
import im.actor.server.bot.{ BotExtension, BotServiceBase }

private[bot] final class WebHooksBotService(system: ActorSystem) extends BotServiceBase(system) with FutureResultCats[BotMessages.BotError] {

  import BotMessages._
  import system.dispatcher

  private implicit val timeout = Timeout(ActorConfig.defaultTimeout)
  private val botExt = BotExtension(system)

  override val handlers: Handlers = {
    case RegisterHook(name: String) ⇒ registerHook(name).toWeak
    case GetHooks                   ⇒ getHooks().toWeak
  }

  private def registerHook(name: String) = RequestHandler[RegisterHook, RegisterHook#Response] {
    (botUserId: BotUserId, botAuthId: BotAuthId) ⇒
      {
        (for {
          _ ← fromFutureBoolean(BotError(406, "HOOK_EXISTS"))(botExt.webHookExists(botUserId, name).map(!_))
          token ← fromFuture(botExt.registerWebHook(botUserId, name))
        } yield Container(botExt.getHookUrl(token))).toEither
      }
  }

  private def getHooks() = RequestHandler[GetHooks, GetHooks#Response] {
    (botUserId: BotUserId, botAuthId: BotAuthId) ⇒
      {
        for {
          hooks ← botExt.findWebHooks(botUserId)
        } yield Right(ContainerList(hooks))
      }
  }
}