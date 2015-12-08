package im.actor.server.file

import akka.actor.ActorSystem
import com.sksamuel.scrimage.{ AsyncImage, Format, Position }
import im.actor.server.acl.ACLUtils
import im.actor.server.db.DbExtension
import im.actor.server.{ model, persist }
import slick.dbio.DBIO

import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

object ImageUtils {
  val AvatarSizeLimit = 1024L * 1024 // TODO: configurable
  val SmallSize = 100
  val LargeSize = 200

  def avatar(ad: model.AvatarData) =
    (ad.smallOpt, ad.largeOpt, ad.fullOpt) match {
      case (None, None, None) ⇒ None
      case (smallOpt, largeOpt, fullOpt) ⇒
        Some(Avatar(
          avatarImage(smallOpt, SmallSize, SmallSize),
          avatarImage(largeOpt, LargeSize, LargeSize),
          avatarImage(fullOpt)
        ))
    }

  def avatarImage(idhashsize: Option[(Long, Long, Long)], width: Int, height: Int): Option[AvatarImage] =
    idhashsize map {
      case (id, hash, size) ⇒ AvatarImage(FileLocation(id, hash), width, height, size)
    }

  def avatarImage(idhashsizewh: Option[(Long, Long, Long, Int, Int)]): Option[AvatarImage] =
    idhashsizewh flatMap {
      case (id, hash, size, w, h) ⇒ avatarImage(Some((id, hash, size)), w, h)
    }

  def resizeTo(aimg: AsyncImage, side: Int)(implicit ec: ExecutionContext): Future[AsyncImage] = {
    for {
      scaledImg ← scaleTo(aimg, side)
      resizedImg ← scaledImg.resizeTo(side, side, Position.Center)
    } yield resizedImg
  }

  def scaleTo(aimg: AsyncImage, side: Int)(implicit ec: ExecutionContext): Future[AsyncImage] = {
    val scaleFactor = side.toDouble / math.min(aimg.width, aimg.height)
    aimg.scale(scaleFactor)
  }

  def dimensions(aimg: AsyncImage)(implicit ec: ExecutionContext): (Int, Int) =
    (aimg.width, aimg.height)

  def scaleStickerF(fullFileId: Long)(
    implicit
    fsAdapter: FileStorageAdapter,
    ec:        ExecutionContext,
    system:    ActorSystem
  ): Future[Either[Throwable, Avatar]] =
    DbExtension(system).db.run(scaleAvatar(fullFileId, ThreadLocalRandom.current(), smallSize = 128, largeSize = 256))

  def scaleAvatarF(fullFileId: Long)(
    implicit
    fsAdapter: FileStorageAdapter,
    ec:        ExecutionContext,
    system:    ActorSystem
  ): Future[Either[Throwable, Avatar]] =
    DbExtension(system).db.run(scaleAvatar(fullFileId))

  def scaleAvatar(fullFileId: Long)(
    implicit
    fsAdapter: FileStorageAdapter,
    ec:        ExecutionContext,
    system:    ActorSystem
  ): DBIO[Either[Throwable, Avatar]] =
    scaleAvatar(fullFileId, ThreadLocalRandom.current())

  def scaleAvatar(
    fullFileId: Long,
    rng:        ThreadLocalRandom
  )(
    implicit
    fsAdapter: FileStorageAdapter,
    ec:        ExecutionContext,
    system:    ActorSystem
  ): DBIO[Either[Throwable, Avatar]] =
    scaleAvatar(fullFileId, rng, SmallSize, LargeSize)

  def scaleAvatar(
    fullFileId: Long,
    rng:        ThreadLocalRandom,
    smallSize:  Int,
    largeSize:  Int
  )(
    implicit
    fsAdapter: FileStorageAdapter,
    ec:        ExecutionContext,
    system:    ActorSystem
  ): DBIO[Either[Throwable, Avatar]] = {
    val smallFileName = "small-avatar.jpg"
    val largeFileName = "large-avatar.jpg"

    persist.FileRepo.find(fullFileId) flatMap {
      case Some(fullFileModel) ⇒
        fsAdapter.downloadFile(fullFileId) flatMap {
          case Some(fullFile) ⇒
            val action = for {
              fullAimg ← DBIO.from(AsyncImage(fullFile))
              (fiw, fih) = dimensions(fullAimg)

              smallAimg ← DBIO.from(resizeTo(fullAimg, smallSize))
              largeAimg ← DBIO.from(resizeTo(fullAimg, largeSize))

              smallFile = fullFile.getParentFile.toPath.resolve(smallFileName).toFile
              largeFile = fullFile.getParentFile.toPath.resolve(largeFileName).toFile

              _ ← DBIO.from(smallAimg.writer(Format.JPEG).write(smallFile))
              _ ← DBIO.from(largeAimg.writer(Format.JPEG).write(largeFile))

              smallFileLocation ← fsAdapter.uploadFile(smallFileName, smallFile)
              largeFileLocation ← fsAdapter.uploadFile(largeFileName, largeFile)
            } yield {
              // TODO: #perf calculate file sizes efficiently

              val smallImage = AvatarImage(
                smallFileLocation,
                smallAimg.width,
                smallAimg.height,
                smallFile.length()
              )

              val largeImage = AvatarImage(
                largeFileLocation,
                largeAimg.width,
                largeAimg.height,
                largeFile.length()
              )

              val fullImage = AvatarImage(
                FileLocation(fullFileId, ACLUtils.fileAccessHash(fullFileId, fullFileModel.accessSalt)),
                fullAimg.width,
                fullAimg.height,
                fullFile.length()
              )

              Avatar(Some(smallImage), Some(largeImage), Some(fullImage))
            }

            action.asTry map {
              case Success(res) ⇒ Right(res)
              case Failure(e)   ⇒ Left(e)
            }
          case None ⇒ DBIO.successful(Left(new Exception("Failed to download file")))
        }
      case None ⇒
        DBIO.successful(Left(new Exception("Cannot find file model")))
    }
  }

  def getAvatar(avatarModel: model.AvatarData): Avatar = {
    val smallImageOpt = avatarModel.smallOpt map {
      case (fileId, fileHash, fileSize) ⇒ AvatarImage(FileLocation(fileId, fileHash), SmallSize, SmallSize, fileSize)
    }

    val largeImageOpt = avatarModel.largeOpt map {
      case (fileId, fileHash, fileSize) ⇒ AvatarImage(FileLocation(fileId, fileHash), LargeSize, LargeSize, fileSize)
    }

    val fullImageOpt = avatarModel.fullOpt map {
      case (fileId, fileHash, fileSize, w, h) ⇒ AvatarImage(FileLocation(fileId, fileHash), w, h, fileSize)
    }

    Avatar(smallImageOpt, largeImageOpt, fullImageOpt)
  }

  def getAvatarData(entityType: model.AvatarData.TypeVal, entityId: Int, avatar: Avatar): model.AvatarData = {
    model.AvatarData(
      entityType = entityType,
      entityId = entityId.toLong,
      smallAvatarFileId = avatar.smallImage map (_.fileLocation.fileId),
      smallAvatarFileHash = avatar.smallImage map (_.fileLocation.accessHash),
      smallAvatarFileSize = avatar.smallImage map (_.fileSize),
      largeAvatarFileId = avatar.largeImage map (_.fileLocation.fileId),
      largeAvatarFileHash = avatar.largeImage map (_.fileLocation.accessHash),
      largeAvatarFileSize = avatar.largeImage map (_.fileSize),
      fullAvatarFileId = avatar.fullImage map (_.fileLocation.fileId),
      fullAvatarFileHash = avatar.fullImage map (_.fileLocation.accessHash),
      fullAvatarFileSize = avatar.fullImage map (_.fileSize),
      fullAvatarWidth = avatar.fullImage map (_.width),
      fullAvatarHeight = avatar.fullImage map (_.height)
    )
  }
}
