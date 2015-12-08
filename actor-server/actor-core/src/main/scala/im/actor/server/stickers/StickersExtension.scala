package im.actor.server.stickers

import akka.actor._
import cats.data.Xor
import im.actor.concurrent.FutureResultCats
import im.actor.server.acl.ACLUtils
import im.actor.server.db.DbExtension
import im.actor.server.file.Avatar
import im.actor.server.model.{ StickerData, StickerPack }
import im.actor.server.persist.{ StickerDataRepo, OwnStickerPackRepo, StickerPackRepo }
import im.actor.server.user.UserExtension
import im.actor.util.misc.IdUtils

import scala.concurrent.Future
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.util.control.NoStackTrace

abstract class StickerError(message: String) extends RuntimeException(message) with NoStackTrace

object StickerErrors {
  case object NoPreview extends StickerError("Got no preview after resize")
  case object NotOwner extends StickerError("Only owner can modify this sticker pack")
  case object NotFound extends StickerError("Sticker pack not found")
  case object NotAdmin extends StickerError("Only admin can perform this action")
  case object AlreadyDefault extends StickerError("Sticker pack is already default")
  case object AlreayNotDefault extends StickerError("Sticker pack is not default already")

  def isDefaultError(isDefault: Boolean): StickerError =
    if (isDefault) AlreadyDefault else AlreayNotDefault
}

sealed trait StickersExtension extends Extension

final class StickersExtensionImpl(_system: ActorSystem) extends StickersExtension with FutureResultCats[StickerError] {

  import StickerErrors._

  implicit val system: ActorSystem = _system
  import system.dispatcher

  private val db = DbExtension(system).db
  private val userExt = UserExtension(system)

  def createPack(creatorUserId: Int, isDefault: Boolean): Future[Int] = {
    val rng = ThreadLocalRandom.current()
    val packId = IdUtils.nextIntId(rng)
    val accessSalt = ACLUtils.nextAccessSalt(rng)
    db.run(for {
      _ ← StickerPackRepo.create(StickerPack(packId, accessSalt, creatorUserId, isDefault))
      _ ← OwnStickerPackRepo.create(creatorUserId, packId)
    } yield packId)
  }

  def isOwner(userId: Int, packId: Int): Future[Boolean] = db.run(StickerPackRepo.exists(userId, packId))

  def addSticker(ownerUserId: Int, packId: Int, emoji: Option[String], resizedSticker: Avatar): Future[StickerError Xor Unit] =
    (for {
      _ ← fromFutureBoolean(NotOwner)(db.run(StickerPackRepo.exists(ownerUserId, packId)) map !=)
      image128 ← fromOption(NoPreview)(resizedSticker.smallImage)
      image256 = resizedSticker.largeImage
      image512 = resizedSticker.fullImage
      sticker = StickerData(id = IdUtils.nextIntId(), packId, emoji,
        image128FileId = image128.fileLocation.fileId,
        image128FileHash = image128.fileLocation.accessHash,
        image128FileSize = image128.fileSize,
        image256FileId = image256 map (_.fileLocation.fileId),
        image256FileHash = image256 map (_.fileLocation.accessHash),
        image256FileSize = image256 map (_.fileSize),
        image512FileId = image512 map (_.fileLocation.fileId),
        image512FileHash = image512 map (_.fileLocation.accessHash),
        image512FileSize = image512 map (_.fileSize))
      _ ← fromFuture(db.run(StickerDataRepo.create(sticker)))
    } yield ()).value

  def getStickerPacks(ownerUserId: Int): Future[Seq[StickerPack]] =
    db.run(StickerPackRepo.findByOwner(ownerUserId))

  def getStickers(ownerUserId: Int, packId: Int): Future[StickerError Xor Seq[StickerData]] =
    (for {
      pack ← fromFutureOption(NotFound)(db.run(StickerPackRepo.find(ownerUserId)))
      _ ← fromBoolean(NotOwner)(pack.ownerUserId == ownerUserId)
      stickers ← fromFuture(db.run(StickerDataRepo.findByPack(packId)))
    } yield stickers).value

  def deleteSticker(ownerUserId: Int, packId: Int, stickerId: Int): Future[StickerError Xor Unit] =
    (for {
      _ ← fromFutureBoolean(NotOwner)(db.run(StickerPackRepo.exists(ownerUserId, packId)) map !=)
      _ ← fromFuture(db.run(StickerDataRepo.delete(packId, stickerId)))
    } yield ()).value

  def makeStickerPackDefault(userId: Int, packId: Int): Future[StickerError Xor Unit] =
    toggleDefault(userId, packId, toggleTo = true)

  def unmakeStickerPackDefault(userId: Int, packId: Int): Future[StickerError Xor Unit] =
    toggleDefault(userId, packId, toggleTo = false)

  private def toggleDefault(userId: Int, packId: Int, toggleTo: Boolean): Future[StickerError Xor Unit] =
    (for {
      _ ← fromFutureBoolean(NotAdmin)(userExt.isAdmin(userId))
      pack ← fromFutureOption(NotFound)(db.run(StickerPackRepo.find(packId)))
      _ ← fromBoolean(isDefaultError(toggleTo))(pack.isDefault != toggleTo)
      _ ← fromFuture(db.run(StickerPackRepo.setDefault(packId, isDefault = toggleTo)))
    } yield ()).value

}

object StickersExtension extends ExtensionId[StickersExtensionImpl] with ExtensionIdProvider {
  override def lookup() = StickersExtension
  override def createExtension(system: ExtendedActorSystem) = new StickersExtensionImpl(system)
}
