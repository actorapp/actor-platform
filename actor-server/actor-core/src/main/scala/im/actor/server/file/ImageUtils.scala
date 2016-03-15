package im.actor.server.file

import java.io.ByteArrayOutputStream

import akka.actor.ActorSystem
import com.sksamuel.scrimage.nio.{ ImageWriter, JpegWriter, PngWriter }
import com.sksamuel.scrimage.{ Image, ParImage, Position }
import im.actor.server.acl.ACLUtils
import im.actor.server.db.DbExtension
import im.actor.server.model.AvatarData
import im.actor.server.persist.files.FileRepo
import im.actor.util.ThreadLocalSecureRandom
import slick.dbio.DBIO

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

object ImageUtils {
  val AvatarSizeLimit = 1024L * 1024 // TODO: configurable
  val SmallSize = 100
  val LargeSize = 200

  private case class ThumbDescriptor(name: String, side: Int, writer: ImageWriter)

  def avatar(ad: AvatarData) =
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

  def resizeTo(aimg: ParImage, side: Int)(implicit ec: ExecutionContext): Future[ParImage] =
    for (scaledImg ← scaleTo(aimg, side)) yield scaledImg.resizeTo(side, side, Position.Center)

  def scaleTo(aimg: ParImage, side: Int)(implicit ec: ExecutionContext): Future[ParImage] = {
    val scaleFactor = side.toDouble / math.min(aimg.width, aimg.height)
    aimg.scale(scaleFactor)
  }

  def dimensions(aimg: ParImage)(implicit ec: ExecutionContext): (Int, Int) =
    (aimg.width, aimg.height)

  def scaleStickerF(fullFileId: Long)(
    implicit
    fsAdapter: FileStorageAdapter,
    ec:        ExecutionContext,
    system:    ActorSystem
  ): Future[Either[Throwable, Avatar]] =
    DbExtension(system).db.run(
      scaleAvatar(
        fullFileId,
        ThreadLocalSecureRandom.current(),
        ThumbDescriptor("small-sticker.png", 128, PngWriter()),
        ThumbDescriptor("medium-sticker.png", 256, PngWriter())
      )
    )

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
    scaleAvatar(fullFileId, ThreadLocalSecureRandom.current())

  def scaleAvatar(
    fullFileId: Long,
    rng:        ThreadLocalSecureRandom
  )(implicit system: ActorSystem): DBIO[Either[Throwable, Avatar]] =
    scaleAvatar(
      fullFileId,
      rng,
      ThumbDescriptor("small-avatar.jpg", SmallSize, JpegWriter()),
      ThumbDescriptor("large-avatar.jpg", LargeSize, JpegWriter())
    )

  def scaleAvatar(
    fullFileId: Long,
    rng:        ThreadLocalSecureRandom,
    smallDesc:  ThumbDescriptor,
    largeDesc:  ThumbDescriptor
  )(implicit system: ActorSystem): DBIO[Either[Throwable, Avatar]] = {
    implicit val ec: ExecutionContext = system.dispatcher
    val fsAdapter = FileStorageExtension(system).fsAdapter
    FileRepo.find(fullFileId) flatMap {
      case Some(fullFileModel) ⇒
        fsAdapter.downloadFile(fullFileId) flatMap {
          case Some(fullFileData) ⇒
            val action = for {
              fullAimg ← Future.fromTry(Try(Image(fullFileData).toPar))
              (fiw, fih) = dimensions(fullAimg)

              smallAimg ← resizeTo(fullAimg, smallDesc.side)
              largeAimg ← resizeTo(fullAimg, largeDesc.side)

              smallBaos = new ByteArrayOutputStream()
              largeBaos = new ByteArrayOutputStream()

              _ ← Future.fromTry(Try(smallAimg.toImage.forWriter(smallDesc.writer).write(smallBaos)))
              _ ← Future.fromTry(Try(largeAimg.toImage.forWriter(largeDesc.writer).write(largeBaos)))

              smallBytes = smallBaos.toByteArray
              largeBytes = largeBaos.toByteArray

              smallFileLocation ← fsAdapter.uploadFileF(UnsafeFileName(smallDesc.name), smallBytes)
              largeFileLocation ← fsAdapter.uploadFileF(UnsafeFileName(largeDesc.name), largeBytes)
            } yield {
              // TODO: #perf calculate file sizes efficiently

              val smallImage = AvatarImage(
                smallFileLocation,
                smallAimg.width,
                smallAimg.height,
                smallBytes.length.toLong
              )

              val largeImage = AvatarImage(
                largeFileLocation,
                largeAimg.width,
                largeAimg.height,
                largeBytes.length.toLong
              )

              val fullImage = AvatarImage(
                FileLocation(fullFileId, ACLUtils.fileAccessHash(fullFileId, fullFileModel.accessSalt)),
                fullAimg.width,
                fullAimg.height,
                fullFileData.length.toLong
              )

              Avatar(Some(smallImage), Some(largeImage), Some(fullImage))
            }

            DBIO.from(action).asTry map {
              case Success(res) ⇒ Right(res)
              case Failure(e)   ⇒ Left(e)
            }
          case None ⇒ DBIO.successful(Left(new Exception("Failed to download file")))
        }
      case None ⇒
        DBIO.successful(Left(new Exception("Cannot find file model")))
    }
  }

  def getAvatar(avatarModel: AvatarData): Avatar = {
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

  def getAvatarData(entityType: AvatarData.TypeVal, entityId: Int, avatar: Avatar): AvatarData = {
    AvatarData(
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
