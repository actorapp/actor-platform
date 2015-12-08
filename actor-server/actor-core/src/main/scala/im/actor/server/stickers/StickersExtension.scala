package im.actor.server.stickers

import akka.actor._
import cats.data.Xor
import im.actor.concurrent.FutureResultCats
import im.actor.server.acl.ACLUtils
import im.actor.server.db.DbExtension
import im.actor.server.file.Avatar
import im.actor.server.model.{ StickerData, StickerPack }
import im.actor.server.persist.{ StickerDataRepo, OwnStickerPackRepo, StickerPackRepo }

import scala.concurrent.Future
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.util.control.NoStackTrace

object StickerErrors {

  abstract class StickerError(message: String) extends RuntimeException(message) with NoStackTrace

  case object NoPreview extends StickerError("Got no preview after resize")
}

sealed trait StickersExtension extends Extension

final class StickersExtensionImpl(_system: ActorSystem) extends StickersExtension with FutureResultCats[Throwable] {

  import StickerErrors._

  implicit val system: ActorSystem = _system
  import system.dispatcher

  private val db = DbExtension(system).db

  def createPack(creatorUserId: Int, isDefault: Boolean): Future[Int] = {
    val rng = ThreadLocalRandom.current()
    val packId = rng.nextInt()
    val accessSalt = ACLUtils.nextAccessSalt(rng)
    db.run(for {
      _ ← StickerPackRepo.create(StickerPack(packId, accessSalt, creatorUserId, isDefault))
      _ ← OwnStickerPackRepo.create(creatorUserId, packId)
    } yield packId)
  }

  def isOwner(userId: Int, packId: Int): Future[Boolean] = db.run(StickerPackRepo.exists(userId, packId))

  def addSticker(userId: Int, packId: Int, emoji: Option[String], resizedSticker: Avatar): Future[Throwable Xor Unit] = {
    val rng = ThreadLocalRandom.current()
    val id = rng.nextInt()
    (for {
      image128 ← fromOption(NoPreview)(resizedSticker.smallImage)
      image256 = resizedSticker.largeImage
      image512 = resizedSticker.fullImage
      sticker = StickerData(id, packId, emoji,
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
  }
}

object StickersExtension extends ExtensionId[StickersExtensionImpl] with ExtensionIdProvider {
  override def lookup() = StickersExtension
  override def createExtension(system: ExtendedActorSystem) = new StickersExtensionImpl(system)
}
