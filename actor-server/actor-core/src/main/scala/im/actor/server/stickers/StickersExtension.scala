package im.actor.server.stickers

import akka.actor._
import cats.data.Xor
import im.actor.api.rpc.stickers.{ ApiStickerCollection, UpdateOwnStickersChanged, UpdateStickerCollectionsChanged }
import im.actor.concurrent.{ FutureExt, FutureResultCats }
import im.actor.server.acl.ACLUtils
import im.actor.server.db.DbExtension
import im.actor.server.model.{ StickerData, StickerPack }
import im.actor.server.persist.{ OwnStickerPackRepo, StickerDataRepo, StickerPackRepo, UserRepo }
import im.actor.server.sequence.SeqUpdatesExtension
import im.actor.server.sticker.Sticker
import im.actor.server.user.UserExtension
import im.actor.util.misc.IdUtils
import slick.dbio.DBIO

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
  case object AlreadyNotDefault extends StickerError("Sticker pack is not default already")

  def isDefaultError(isDefault: Boolean): StickerError =
    if (isDefault) AlreadyDefault else AlreadyNotDefault
}

sealed trait StickersExtension extends Extension

final class StickersExtensionImpl(_system: ActorSystem)
  extends StickersExtension
  with FutureResultCats[StickerError]
  with StickersImplicitConversions {

  StickerMessages.register()

  import StickerErrors._

  implicit val system: ActorSystem = _system
  import system.dispatcher

  private val db = DbExtension(system).db
  private val userExt = UserExtension(system)
  private val seqExt = SeqUpdatesExtension(system)

  def createPack(creatorUserId: Int, isDefault: Boolean): Future[Int] = {
    val rng = ThreadLocalRandom.current()
    val packId = IdUtils.nextIntId(rng)
    val accessSalt = ACLUtils.nextAccessSalt(rng)
    db.run(for {
      _ ← StickerPackRepo.create(StickerPack(packId, accessSalt, creatorUserId, isDefault))
      _ ← OwnStickerPackRepo.create(creatorUserId, packId)
      collections ← getOwnApiStickerPacks(creatorUserId)
      _ ← DBIO.from(seqExt.deliverSingleUpdate(creatorUserId, UpdateOwnStickersChanged(collections)))
    } yield packId)
  }

  def isOwner(userId: Int, packId: Int): Future[Boolean] = db.run(StickerPackRepo.exists(userId, packId))

  def addSticker(ownerUserId: Int, packId: Int, emoji: Option[String], resizedSticker: Sticker): Future[StickerError Xor Unit] =
    (for {
      pack ← fromFutureOption(NotFound)(db.run(StickerPackRepo.find(packId)))
      _ ← fromBoolean(NotFound)(pack.ownerUserId == ownerUserId)
      image128 ← fromOption(NoPreview)(resizedSticker.small)
      image256 = resizedSticker.medium
      image512 = resizedSticker.large
      sticker = StickerData(id = IdUtils.nextIntId(), packId, emoji,
        image128FileId = image128.fileLocation.fileId,
        image128FileHash = image128.fileLocation.accessHash,
        image128FileSize = image128.fileSize,
        image128Width = image128.width,
        image128Height = image128.height,
        image256FileId = image256 map (_.fileLocation.fileId),
        image256FileHash = image256 map (_.fileLocation.accessHash),
        image256FileSize = image256 map (_.fileSize),
        image256Width = image256 map (_.width),
        image256Height = image256 map (_.height),
        image512FileId = image512 map (_.fileLocation.fileId),
        image512FileHash = image512 map (_.fileLocation.accessHash),
        image512FileSize = image512 map (_.fileSize),
        image512Width = image512 map (_.width),
        image512Height = image512 map (_.height))
      _ ← fromFuture(db.run(StickerDataRepo.create(sticker)))
      _ = deliverStickerCollectionChanged(pack)
    } yield ()).value

  def getStickerPacks(ownerUserId: Int): Future[Seq[StickerPack]] =
    db.run(StickerPackRepo.findByOwner(ownerUserId))

  def getStickers(ownerUserId: Int, packId: Int): Future[StickerError Xor Seq[StickerData]] =
    (for {
      pack ← fromFutureOption(NotFound)(db.run(StickerPackRepo.find(packId)))
      _ ← fromBoolean(NotOwner)(pack.ownerUserId == ownerUserId)
      stickers ← fromFuture(db.run(StickerDataRepo.findByPack(packId)))
    } yield stickers).value

  def deleteSticker(ownerUserId: Int, packId: Int, stickerId: Int): Future[StickerError Xor Unit] =
    (for {
      pack ← fromFutureOption(NotFound)(db.run(StickerPackRepo.find(packId)))
      _ ← fromBoolean(NotFound)(pack.ownerUserId == ownerUserId)
      _ ← fromFuture(db.run(StickerDataRepo.delete(packId, stickerId)))
      _ = deliverStickerCollectionChanged(pack)
    } yield ()).value

  def getPackUserIds(pack: StickerPack): DBIO[Seq[Int]] =
    if (pack.isDefault)
      UserRepo.activeUsersIds
    else OwnStickerPackRepo.findUserIds(pack.id)

  def getApiStickerPack(pack: StickerPack): DBIO[ApiStickerCollection] =
    for {
      stickers ← StickerDataRepo.findByPack(pack.id)
    } yield ApiStickerCollection(pack.id, ACLUtils.stickerPackAccessHash(pack), stickers)

  def getOwnApiStickerPacks(userId: Int): DBIO[Vector[ApiStickerCollection]] =
    for {
      packIds ← OwnStickerPackRepo.findPackIds(userId)
      packs ← StickerPackRepo.find(packIds)
      stickerCollections ← DBIO.sequence(packs.toVector map { pack ⇒
        for (stickers ← StickerDataRepo.findByPack(pack.id)) yield ApiStickerCollection(pack.id, ACLUtils.stickerPackAccessHash(pack), stickers)
      })
    } yield stickerCollections

  def makeStickerPackDefault(userId: Int, packId: Int): Future[StickerError Xor Unit] =
    toggleDefault(userId, packId, toggleTo = true)

  def unmakeStickerPackDefault(userId: Int, packId: Int): Future[StickerError Xor Unit] =
    toggleDefault(userId, packId, toggleTo = false)

  private def toggleDefault(userId: Int, packId: Int, toggleTo: Boolean): Future[StickerError Xor Unit] =
    (for {
      isAdmin ← fromFuture(userExt.isAdmin(userId))
      _ = system.log.debug("user: {} is admin: {}", userId, isAdmin)
      _ ← fromBoolean(NotAdmin)(isAdmin)
      pack ← fromFutureOption(NotFound)(db.run(StickerPackRepo.find(packId)))
      _ = system.log.debug("sticker pack: {}", pack)
      _ ← fromBoolean(isDefaultError(toggleTo))(pack.isDefault != toggleTo)
      _ ← fromFuture(db.run(StickerPackRepo.setDefault(packId, isDefault = toggleTo)))
      _ = broadcastOwnStickersChanged()
    } yield ()).value

  /**
   * Broadcast `UpdateOwnStickersChanged` to all users.
   * This will happen when admin makes/unmakes sticker pack default
   */
  private def broadcastOwnStickersChanged(): Future[Unit] = for {
    allUsersIds ← db.run(UserRepo.activeUsersIds)
    _ ← FutureExt.ftraverse(allUsersIds) { uid ⇒
      db.run(getOwnApiStickerPacks(uid)) flatMap { packs ⇒
        seqExt.deliverSingleUpdate(uid, UpdateOwnStickersChanged(packs))
      }
    }
  } yield ()

  /**
   * Deliver `UpdateStickerCollectionsChanged` to those users,
   * who have given sticker pack in their sticker collection
   *
   * @param pack sticker pack that changed
   */
  private def deliverStickerCollectionChanged(pack: StickerPack): Future[Unit] = for {
    packUserIds ← db.run(getPackUserIds(pack))
    apiPack ← db.run(getApiStickerPack(pack))
    _ ← FutureExt.ftraverse(packUserIds) { uid ⇒
      seqExt.deliverSingleUpdate(uid, UpdateStickerCollectionsChanged(Vector(apiPack)))
    }
  } yield ()

}

object StickersExtension extends ExtensionId[StickersExtensionImpl] with ExtensionIdProvider {
  override def lookup() = StickersExtension
  override def createExtension(system: ExtendedActorSystem) = new StickersExtensionImpl(system)
}
