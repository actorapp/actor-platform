package im.actor.server.bot.services

import akka.actor.ActorSystem
import im.actor.api.rpc.RpcError
import im.actor.bots.BotMessages.BotError
import im.actor.concurrent.FutureResultCats
import im.actor.server.bot.{ ApiToBotConversions, BotServiceBase }
import im.actor.server.db.DbExtension
import im.actor.server.file.{ S3StorageExtension, FileStorageAdapter }

final class UsersBotService(system: ActorSystem) extends BotServiceBase(system) with FutureResultCats[BotError] with ApiToBotConversions {
  import im.actor.bots.BotMessages._
  import system.dispatcher
  import im.actor.server.file.ImageUtils._

  private val db = DbExtension(system).db
  private implicit val fsAdapter: FileStorageAdapter = S3StorageExtension(system).s3StorageAdapter
  private implicit val _system = system

  override val handlers: Handlers = {
    case ChangeUserAvatar(userId, fileLocation) ⇒ changeUserAvatar(userId, fileLocation).toWeak
    case ChangeUserName(userId, name)           ⇒ changeUserName(userId, name).toWeak
    case ChangeUserAbout(userId, about)         ⇒ changeUserAbout(userId, about).toWeak
  }

  private def changeUserName(userId: Int, name: String) = RequestHandler[ChangeUserName, ChangeUserName#Response] {
    (botUserId: BotUserId, botAuthId: BotAuthId) ⇒
      ifIsAdmin(botUserId) {
        (for {
          _ ← fromFuture(userExt.changeName(userId, name))
        } yield Void).value
      }
  }

  private def changeUserAbout(userId: Int, about: Option[String]) = RequestHandler[ChangeUserAbout, ChangeUserAbout#Response] {
    (botUserId: BotUserId, botAuthId: BotAuthId) ⇒
      ifIsAdmin(botUserId) {
        (for {
          _ ← fromFuture(userExt.changeAbout(userId, 0, about))
        } yield Void).value
      }
  }

  private def changeUserAvatar(userId: Int, fileLocation: FileLocation) = RequestHandler[ChangeUserAvatar, ChangeUserAvatar#Response] {
    (botUserId: BotUserId, botAuthId: BotAuthId) ⇒
      ifIsAdmin(botUserId) {
        (for {
          avatar ← fromFutureEither(_ ⇒ BotError(400, "LOCATION_INVALID"))(db.run(scaleAvatar(fileLocation.fileId)))
          _ ← fromFuture(userExt.updateAvatar(userId, 0, Some(avatar)))
        } yield Void).value
      }
  }
}