package im.actor.server.bot.services

import akka.actor.ActorSystem
import im.actor.bots.BotMessages._
import im.actor.concurrent.FutureResultCats
import im.actor.server.bot.{ ApiToBotConversions, BotServiceBase }
import im.actor.server.file.{ S3StorageExtension, S3StorageAdapter, ImageUtils }
import im.actor.server.stickers.{ StickerErrors, StickersExtension }

private[bot] object StickersBotErrors {
  val LocationInvalid = BotError(400, "LOCATION_INVALID")
  val NotAllowedToEdit = BotError(403, "NOT_ALLOWED_TO_EDIT")
  val FailedToMakePreview = BotError(500, "FAILED_MAKE_PREVIEW")
  val StickerNotFound = BotError(404, "NOT_FOUND")
  val UserNotAdmin = BotError(403, "FORBIDDEN")
  val AlreadyDefault = BotError(400, "ALREADY_DEFAULT")
  val AlreadyNotDefault = BotError(400, "ALREADY_NOT_DEFAULT")

  def catchStickerErrors: PartialFunction[Throwable, BotError] = {
    case StickerErrors.NoPreview        ⇒ FailedToMakePreview
    case StickerErrors.NotOwner         ⇒ NotAllowedToEdit
    case StickerErrors.NotFound         ⇒ StickerNotFound
    case StickerErrors.NotAdmin         ⇒ UserNotAdmin
    case StickerErrors.AlreadyDefault   ⇒ AlreadyDefault
    case StickerErrors.AlreayNotDefault ⇒ AlreadyNotDefault
    case _                              ⇒ BotError(500, "INTERNAL_ERROR")
  }
}

private[bot] final class StickersBotService(_system: ActorSystem) extends BotServiceBase(_system) with FutureResultCats[BotError] with ApiToBotConversions {

  import ImageUtils._
  import StickersBotErrors._

  private implicit val system: ActorSystem = _system
  import system.dispatcher

  private val stickerExt = StickersExtension(system)
  private implicit val fsAdapter: S3StorageAdapter = S3StorageExtension(system).s3StorageAdapter

  override def handlers: Handlers = {
    case CreateStickerPack(userId)                            ⇒ createStickerPack(userId).toWeak
    case AddSticker(ownerUserId, packId, emoji, fileLocation) ⇒ addSticker(ownerUserId, packId, emoji, fileLocation).toWeak
    case ShowStickerPacks(ownerUserId)                        ⇒ showStickerPacks(ownerUserId).toWeak
    case ShowStickers(ownerUserId, packId)                    ⇒ showStickers(ownerUserId, packId).toWeak
    case DeleteSticker(ownerUserId, packId, stickerId)        ⇒ deleteSticker(ownerUserId, packId, stickerId).toWeak

    //!!!requires admin rights from peer user!!!
    case MakeStickerPackDefault(userId, packId)               ⇒ makeStickerPackDefault(userId, packId).toWeak
    case UnmakeStickerPackDefault(userId, packId)             ⇒ unmakeStickerPackDefault(userId, packId).toWeak
  }

  private def createStickerPack(userId: Int) = RequestHandler[CreateStickerPack, CreateStickerPack#Response] {
    (botUserId: BotUserId, botAuthId: BotAuthId, botAuthSid: BotAuthSid) ⇒
      ifIsAdmin(botUserId) {
        (for {
          packId ← fromFuture(stickerExt.createPack(userId, isDefault = false))
        } yield Container(packId.toString)).value
      }
  }

  private def addSticker(ownerUserId: Int, packId: Int, emoji: Option[String], fileLocation: FileLocation) = RequestHandler[AddSticker, AddSticker#Response] {
    (botUserId: BotUserId, botAuthId: BotAuthId, botAuthSid: BotAuthSid) ⇒
      ifIsAdmin(botUserId) {
        (for {
          _ ← fromFutureBoolean(NotAllowedToEdit)(stickerExt.isOwner(ownerUserId, packId))
          sticker ← fromFutureEither(_ ⇒ LocationInvalid)(scaleStickerF(fileLocation.fileId))
          _ ← fromFutureXor(catchStickerErrors)(stickerExt.addSticker(ownerUserId, packId, emoji, sticker))
        } yield Void).value
      }
  }

  def showStickerPacks(ownerUserId: Int) = RequestHandler[ShowStickerPacks, ShowStickerPacks#Response] {
    (botUserId: BotUserId, botAuthId: BotAuthId, botAuthSid: BotAuthSid) ⇒
      ifIsAdmin(botUserId) {
        (for {
          packs ← fromFuture(stickerExt.getStickerPacks(ownerUserId))
        } yield StickerPackIds(packs map (_.id.toString))).value
      }
  }

  def showStickers(ownerUserId: Int, packId: Int) = RequestHandler[ShowStickers, ShowStickers#Response] {
    (botUserId: BotUserId, botAuthId: BotAuthId, botAuthSid: BotAuthSid) ⇒
      ifIsAdmin(botUserId) {
        (for {
          stickers ← fromFutureXor(catchStickerErrors)(stickerExt.getStickers(ownerUserId, packId))
        } yield StickerIds(stickers map (_.id.toString))).value
      }
  }

  def deleteSticker(ownerUserId: Int, packId: Int, stickerId: Int) = RequestHandler[DeleteSticker, DeleteSticker#Response] {
    (botUserId: BotUserId, botAuthId: BotAuthId, botAuthSid: BotAuthSid) ⇒
      ifIsAdmin(botUserId) {
        (for {
          _ ← fromFutureXor(catchStickerErrors)(stickerExt.deleteSticker(ownerUserId, packId, stickerId))
        } yield Void).value
      }
  }

  def makeStickerPackDefault(userId: Int, packId: Int) = RequestHandler[MakeStickerPackDefault, MakeStickerPackDefault#Response] {
    (botUserId: BotUserId, botAuthId: BotAuthId, botAuthSid: BotAuthSid) ⇒
      ifIsAdmin(botUserId) {
        ifIsAdmin(userId) {
          (for {
            _ ← fromFutureXor(catchStickerErrors)(stickerExt.makeStickerPackDefault(userId, packId))
          } yield Void).value
        }
      }
  }

  def unmakeStickerPackDefault(userId: Int, packId: Int) = RequestHandler[UnmakeStickerPackDefault, UnmakeStickerPackDefault#Response] {
    (botUserId: BotUserId, botAuthId: BotAuthId, botAuthSid: BotAuthSid) ⇒
      ifIsAdmin(botUserId) {
        ifIsAdmin(userId) {
          (for {
            _ ← fromFutureXor(catchStickerErrors)(stickerExt.unmakeStickerPackDefault(userId, packId))
          } yield Void).value
        }
      }
  }

}
