package im.actor.server.bot.services

import akka.actor.ActorSystem
import cats.data.Xor
import im.actor.bots.BotMessages
import im.actor.bots.BotMessages.BotError
import im.actor.concurrent.FutureResult
import im.actor.server.bot.{ ApiToBotConversions, BotServiceBase }
import im.actor.server.db.DbExtension
import im.actor.server.file.{ FileStorageAdapter, FileStorageExtension, ImageUtils }
import im.actor.server.user.{ UserErrors, UserExt, UserUtils }

private[bot] final class UsersBotService(system: ActorSystem) extends BotServiceBase(system) with FutureResult[BotError] with ApiToBotConversions {
  import im.actor.bots.BotMessages._
  import system.dispatcher
  import ImageUtils._
  import im.actor.concurrent.FutureExt._

  private val db = DbExtension(system).db
  private implicit val fsAdapter: FileStorageAdapter = FileStorageExtension(system).fsAdapter
  private implicit val _system = system

  override val handlers: Handlers = {
    case ChangeUserAvatar(userId, fileLocation)   ⇒ changeUserAvatar(userId, fileLocation).toWeak
    case ChangeUserName(userId, name)             ⇒ changeUserName(userId, name).toWeak
    case ChangeUserNickname(userId, nickname)     ⇒ changeUserNickname(userId, nickname).toWeak
    case ChangeUserAbout(userId, about)           ⇒ changeUserAbout(userId, about).toWeak
    case AddSlashCommand(userId, command)         ⇒ addSlashCommand(userId, command).toWeak
    case RemoveSlashCommand(userId, slashCommand) ⇒ removeSlashCommand(userId, slashCommand).toWeak
    case AddUserExtString(userId, key, value)     ⇒ addUserExtString(userId, key, value).toWeak
    case AddUserExtBool(userId, key, value)       ⇒ addUserExtBool(userId, key, value).toWeak
    case RemoveUserExt(userId, key)               ⇒ removeUserExt(userId, key).toWeak
    case FindUser(query)                          ⇒ findUser(query).toWeak
    case IsAdmin(userId)                          ⇒ isAdmin(userId).toWeak
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
          avatar ← fromFutureXor(_ ⇒ BotError(400, "LOCATION_INVALID"))(db.run(scaleAvatar(fileLocation.fileId)) map Xor.fromEither)
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

  private def addSlashCommand(userId: Int, command: BotCommand) = RequestHandler[AddSlashCommand, AddSlashCommand#Response] {
    (botUserId: BotUserId, botAuthId: BotAuthId, botAuthSid: BotAuthSid) ⇒
      ifIsAdmin(botUserId) {
        (for {
          _ ← fromFuture(handleBotCommandErrors)(userExt.addBotCommand(userId, command))
        } yield Void).value
      }
  }

  private def removeSlashCommand(userId: Int, slashCommand: String) = RequestHandler[RemoveSlashCommand, RemoveSlashCommand#Response] {
    (botUserId: BotUserId, botAuthId: BotAuthId, botAuthSid: BotAuthSid) ⇒
      ifIsAdmin(botUserId) {
        (for {
          _ ← fromFuture(handleBotCommandErrors)(userExt.removeBotCommand(userId, slashCommand))
        } yield Void).value
      }
  }

  private def addUserExtString(userId: Int, key: String, value: String) = RequestHandler[AddUserExtString, AddUserExtString#Response] {
    (botUserId: BotUserId, botAuthId: BotAuthId, botAuthSid: BotAuthSid) ⇒
      ifIsAdmin(botUserId) {
        (for {
          _ ← fromFuture(handleBotCommandErrors)(userExt.addExt(userId, UserExt(key).withValue(UserExt.Value.StringValue(value))))
        } yield Void).value
      }
  }

  private def addUserExtBool(userId: Int, key: String, value: Boolean) = RequestHandler[AddUserExtString, AddUserExtString#Response] {
    (botUserId: BotUserId, botAuthId: BotAuthId, botAuthSid: BotAuthSid) ⇒
      ifIsAdmin(botUserId) {
        (for {
          _ ← fromFuture(handleBotCommandErrors)(userExt.addExt(userId, UserExt(key).withValue(UserExt.Value.BoolValue(value))))
        } yield Void).value
      }
  }

  private def removeUserExt(userId: Int, key: String) = RequestHandler[AddUserExtString, AddUserExtString#Response] {
    (botUserId: BotUserId, botAuthId: BotAuthId, botAuthSid: BotAuthSid) ⇒
      ifIsAdmin(botUserId) {
        (for {
          _ ← fromFuture(handleBotCommandErrors)(userExt.removeExt(userId, key))
        } yield Void).value
      }
  }

  private def handleBotCommandErrors: PartialFunction[Throwable, BotMessages.BotError] = {
    case UserErrors.InvalidBotCommand(_)       ⇒ BotError(400, "INVALID_SLASH_COMMAND")
    case UserErrors.BotCommandAlreadyExists(_) ⇒ BotError(400, "SLASH_COMMAND_EXISTS")
  }

}