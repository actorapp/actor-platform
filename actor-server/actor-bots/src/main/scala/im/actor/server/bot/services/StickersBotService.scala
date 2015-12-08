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
}

private[bot] final class StickersBotService(_system: ActorSystem) extends BotServiceBase(_system) with FutureResultCats[BotError] with ApiToBotConversions {

  import ImageUtils._
  import StickersBotErrors._

  private implicit val system: ActorSystem = _system
  import system.dispatcher

  private val stickerExt = StickersExtension(system)
  private implicit val fsAdapter: S3StorageAdapter = S3StorageExtension(system).s3StorageAdapter

  override def handlers: Handlers = {
    case CreateStickerPack                       ⇒ createStickerPack().toWeak
    case AddSticker(packId, emoji, fileLocation) ⇒ addSticker(packId, emoji, fileLocation).toWeak
  }

  private def createStickerPack() = RequestHandler[CreateStickerPack, CreateStickerPack#Response] {
    (botUserId: BotUserId, botAuthId: BotAuthId, botAuthSid: BotAuthSid) ⇒
      ifIsAdmin(botUserId) {
        (for {
          packId ← fromFuture(stickerExt.createPack(botUserId, isDefault = false))
        } yield Container(packId)).value
      }
  }

  private def addSticker(packId: Int, emoji: Option[String], fileLocation: FileLocation) = RequestHandler[AddSticker, AddSticker#Response] {
    (botUserId: BotUserId, botAuthId: BotAuthId, botAuthSid: BotAuthSid) ⇒
      ifIsAdmin(botUserId) {
        (for {
          _ ← fromFutureBoolean(NotAllowedToEdit)(stickerExt.isOwner(botUserId, packId))
          sticker ← fromFutureEither(_ ⇒ LocationInvalid)(scaleStickerF(fileLocation.fileId))
          _ ← fromFutureXor({ case StickerErrors.NoPreview ⇒ FailedToMakePreview })(stickerExt.addSticker(botUserId, packId, emoji, sticker))
        } yield Void).value
      }
  }

}
