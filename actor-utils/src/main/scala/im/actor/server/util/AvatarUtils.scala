package im.actor.server.util

import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

import akka.actor.ActorSystem
import com.amazonaws.services.s3.transfer.TransferManager
import com.sksamuel.scrimage.{ AsyncImage, Format, Position }
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.files
import im.actor.api.rpc.files.{ Avatar, AvatarImage, FileLocation }
import im.actor.server.models.AvatarData
import im.actor.server.{ models, persist }

object AvatarUtils {
  import FileUtils._

  val AvatarSizeLimit = 1024 * 1024 // TODO: configurable
  val SmallSize = 100
  val LargeSize = 200

  def avatar(ad: models.AvatarData) =
    (ad.smallOpt, ad.largeOpt, ad.fullOpt) match {
      case (None, None, None) ⇒ None
      case (smallOpt, largeOpt, fullOpt) ⇒
        Some(files.Avatar(
          avatarImage(smallOpt, SmallSize, SmallSize),
          avatarImage(largeOpt, LargeSize, LargeSize),
          avatarImage(fullOpt)
        ))
    }

  def avatarImage(idhashsize: Option[(Long, Long, Int)], width: Int, height: Int): Option[files.AvatarImage] =
    idhashsize map {
      case (id, hash, size) ⇒ files.AvatarImage(files.FileLocation(id, hash), width, height, size)
    }

  def avatarImage(idhashsizewh: Option[(Long, Long, Int, Int, Int)]): Option[files.AvatarImage] =
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

  def resizeToSmall(aimg: AsyncImage)(implicit ec: ExecutionContext): Future[AsyncImage] = resizeTo(aimg, SmallSize)

  def resizeToLarge(aimg: AsyncImage)(implicit ec: ExecutionContext): Future[AsyncImage] = resizeTo(aimg, LargeSize)

  def dimensions(aimg: AsyncImage)(implicit ec: ExecutionContext): (Int, Int) =
    (aimg.width, aimg.height)

  def scaleAvatar(
    fullFileId: Long,
    rnd:        ThreadLocalRandom,
    bucketName: String
  )(
    implicit
    transferManager: TransferManager,
    db:              Database,
    ec:              ExecutionContext,
    system:          ActorSystem
  ) = {
    val smallFileName = "small-avatar.jpg"
    val largeFileName = "large-avatar.jpg"

    persist.File.find(fullFileId) flatMap {
      case Some(fullFileModel) ⇒
        downloadFile(bucketName, fullFileId) flatMap {
          case Some(fullFile) ⇒
            val action = for {
              fullAimg ← DBIO.from(AsyncImage(fullFile))
              (fiw, fih) = dimensions(fullAimg)

              smallAimg ← DBIO.from(resizeToSmall(fullAimg))
              largeAimg ← DBIO.from(resizeToLarge(fullAimg))

              smallFile = fullFile.getParentFile.toPath.resolve(smallFileName).toFile
              largeFile = fullFile.getParentFile.toPath.resolve(largeFileName).toFile

              _ ← DBIO.from(smallAimg.writer(Format.JPEG).write(smallFile))
              _ ← DBIO.from(largeAimg.writer(Format.JPEG).write(largeFile))

              smallFileLocation ← uploadFile(bucketName, smallFileName, smallFile)
              largeFileLocation ← uploadFile(bucketName, largeFileName, largeFile)
            } yield {
              // TODO: #perf calculate file sizes efficiently

              val smallImage = AvatarImage(
                smallFileLocation,
                smallAimg.width,
                smallAimg.height,
                smallFile.length().toInt
              )

              val largeImage = AvatarImage(
                largeFileLocation,
                largeAimg.width,
                largeAimg.height,
                largeFile.length().toInt
              )

              val fullImage = AvatarImage(
                FileLocation(fullFileId, ACLUtils.fileAccessHash(fullFileId, fullFileModel.accessSalt)),
                fullAimg.width,
                fullAimg.height,
                fullFile.length().toInt
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

  def getAvatar(avatarModel: models.AvatarData): Avatar = {
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

  def getAvatarData(entityType: models.AvatarData.TypeVal, entityId: Int, avatar: Avatar): AvatarData = {
    models.AvatarData(
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
