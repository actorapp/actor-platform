package im.actor.server.bot.services

import akka.actor.ActorSystem
import im.actor.bots.BotMessages
import im.actor.server.bot.{ BotExtension, BotServiceBase }
import im.actor.server.user.UserExceptions
import upickle.Js

final class BotsService(system: ActorSystem) extends BotServiceBase {
  import BotMessages._
  import BotServiceBase._
  import system.dispatcher

  val botExt = BotExtension(system)

  private def createBot(username: String, name: String) = RequestHandler[CreateBot, CreateBot#Response](
    (botUserId: BotUserId, botAuthId: BotAuthId) ⇒
      (for {
        token ← botExt.create(username, name, isAdmin = false)
      } yield Right(Container(token))) recover {
        case UserExceptions.NicknameTaken ⇒
          Left(BotError(400, "USERNAME_TAKEN", Js.Obj(), None))
      }
  )

  override def handlers: PartialFunction[RequestBody, WeakRequestHandler] = {
    case CreateBot(username, name) ⇒ createBot(username, name).toWeak
  }
}
