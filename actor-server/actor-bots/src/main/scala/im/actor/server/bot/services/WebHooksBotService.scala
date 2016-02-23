package im.actor.server.bot.services

import akka.actor.ActorSystem
import akka.util.Timeout
import im.actor.bots.BotMessages
import im.actor.concurrent.FutureResult
import im.actor.config.ActorConfig
import im.actor.server.bot.{ BotExtension, BotServiceBase }

private[bot] final class WebHooksBotService(system: ActorSystem) extends BotServiceBase(system) with FutureResult[BotMessages.BotError] {

  import BotMessages._
  import system.dispatcher

  private implicit val timeout = Timeout(ActorConfig.defaultTimeout)
  private val botExt = BotExtension(system)

  override val handlers: Handlers = {
    case RegisterHook(name: String) ⇒ registerHook(name).toWeak
    case GetHooks                   ⇒ getHooks().toWeak
  }

  private def registerHook(name: String) = RequestHandler[RegisterHook, RegisterHook#Response] {
    (botUserId: BotUserId, botAuthId: BotAuthId, botAuthSid: BotAuthSid) ⇒
      {
        (for {
          optToken ← fromFuture(botExt.findToken(botUserId, name))
          token ← optToken map point getOrElse fromFuture(botExt.registerWebHook(botUserId, name))
        } yield Container(botExt.getHookUrl(token))).toEither
      }
  }

  private def getHooks() = RequestHandler[GetHooks, GetHooks#Response] {
    (botUserId: BotUserId, botAuthId: BotAuthId, botAuthSid: BotAuthSid) ⇒
      {
        for {
          hooks ← botExt.findWebHooks(botUserId)
        } yield Right(ContainerList(hooks))
      }
  }
}