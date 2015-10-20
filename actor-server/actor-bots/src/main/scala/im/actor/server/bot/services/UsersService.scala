package im.actor.server.bot.services

import akka.actor.ActorSystem
import im.actor.server.bot.BotServiceBase

final class UsersService(system: ActorSystem) extends BotServiceBase(system) {
  import im.actor.bots.BotMessages._
  import system.dispatcher

  override val handlers: Handlers = {
    case UpdateAvatar(userId, avatar) ⇒ updateAvatar(userId, avatar).toWeak
  }

  private def updateAvatar(userId: Int, avatar: Avatar) = RequestHandler[UpdateAvatar, UpdateAvatar#Response] {
    (botUserId: BotUserId, botAuthId: BotAuthId) ⇒
      ifIsAdmin(botUserId) {
        for {
          _ ← userExt.updateAvatar(userId, 0, Some(avatar))
        } yield Right(Void)
      }
  }
}