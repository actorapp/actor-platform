package im.actor.server.bot.services

import akka.actor.ActorSystem
import cats.data.Xor
import im.actor.bots.BotMessages.BotError
import im.actor.concurrent.FutureResultCats
import im.actor.server.bot.{ ApiToBotConversions, BotServiceBase }
import im.actor.server.db.DbExtension
import im.actor.server.file.{ ImageUtils, FileStorageExtension, FileStorageAdapter }
import im.actor.server.user.{ UserErrors, UserUtils }

private[bot] final class UsersBotService(system: ActorSystem) extends BotServiceBase(system) with FutureResultCats[BotError] with ApiToBotConversions {
  import im.actor.bots.BotMessages._
  import system.dispatcher
  import ImageUtils._
  import im.actor.concurrent.FutureExt._

  private val db = DbExtension(system).db
  private implicit val fsAdapter: FileStorageAdapter = FileStorageExtension(system).fsAdapter
  private implicit val _system = system

  override val handlers: Handlers = {
    case ChangeUserAvatar(userId, fileLocation) ⇒ changeUserAvatar(userId, fileLocation).toWeak
    case ChangeUserName(userId, name)           ⇒ changeUserName(userId, name).toWeak
    case ChangeUserNickname(userId, nickname)   ⇒ changeUserNickname(userId, nickname).toWeak
    case ChangeUserAbout(userId, about)         ⇒ changeUserAbout(userId, about).toWeak
    case FindUser(query)                        ⇒ findUser(query).toWeak
    case IsAdmin(userId)                        ⇒ isAdmin(userId).toWeak
  }

  private def changeUserName(userId: Int, name: String) = RequestHandler[ChangeUserName, ChangeUserName#Response] {
    (botUserId: BotUserId, botAuthId: BotAuthId, botAuthSid: BotAuthSid) ⇒
      ifIsAdmin(botUserId) {
        (for {
          _ ← fromFuture(userExt.changeName(userId, name))
        } yield Void).value
      }
  }

  private def changeUserNickname(userId: Int, nickname: Option[String]) = RequestHandler[ChangeUserNickname, ChangeUserNickname#Response] {
    (botUserId: BotUserId, botAuthId: BotAuthId, botAuthSid: BotAuthSid) ⇒
      ifIsAdmin(botUserId) {
        (for {
          _ ← fromFuture(userExt.changeNickname(userId, nickname))
        } yield Void).value recover {
          case UserErrors.InvalidNickname ⇒ Xor.left(BotError(400, "INVALID_USERNAME"))
          case UserErrors.NicknameTaken   ⇒ Xor.left(BotError(400, "USERNAME_TAKEN"))
        }
      }
  }

  private def changeUserAbout(userId: Int, about: Option[String]) = RequestHandler[ChangeUserAbout, ChangeUserAbout#Response] {
    (botUserId: BotUserId, botAuthId: BotAuthId, botAuthSid: BotAuthSid) ⇒
      ifIsAdmin(botUserId) {
        (for {
          _ ← fromFuture(userExt.changeAbout(userId, about))
        } yield Void).value
      }
  }

  private def changeUserAvatar(userId: Int, fileLocation: FileLocation) = RequestHandler[ChangeUserAvatar, ChangeUserAvatar#Response] {
    (botUserId: BotUserId, botAuthId: BotAuthId, botAuthSid: BotAuthSid) ⇒
      ifIsAdmin(botUserId) {
        (for {
          avatar ← fromFutureEither(_ ⇒ BotError(400, "LOCATION_INVALID"))(db.run(scaleAvatar(fileLocation.fileId)))
          _ ← fromFuture(userExt.updateAvatar(userId, Some(avatar)))
        } yield Void).value
      }
  }

  private def findUser(query: String) = RequestHandler[FindUser, FindUser#Response] {
    (botUserId, botAuthId, botAuthSid) ⇒
      ifIsAdmin(botUserId) {

        (for {
          ids ← fromFuture(userExt.findUserIds(query))
          users ← fromFuture(ftraverse(ids)(UserUtils.safeGetUser(_, botUserId, botAuthId))) map (_.flatten)
        } yield FoundUsers(users)).value
      }
  }

  private def isAdmin(userId: Int) = RequestHandler[IsAdmin, IsAdmin#Response] {
    (botUserId, botAuthId, botAuthSid) ⇒
      ifIsAdmin(botUserId) {
        (for {
          isAdmin ← fromFuture(userExt.isAdmin(userId))
        } yield ResponseIsAdmin(isAdmin)).value
      }
  }
}