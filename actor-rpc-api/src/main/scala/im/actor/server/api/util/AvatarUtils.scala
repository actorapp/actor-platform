package im.actor.server.api.util

import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Success, Failure }

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

  def avatar(ad: models.AvatarData) =
    (ad.smallOpt, ad.largeOpt, ad.fullOpt) match {
      case (None, None, None) ⇒ None
      case (smallOpt, largeOpt, fullOpt) ⇒
        Some(files.Avatar(
          avatarImage(smallOpt, 100, 100),
          avatarImage(largeOpt, 200, 200),
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
    val scaleFactor = side.toDouble / math.min(aimg.width, aimg.height)

    for {
      scaledImg ← aimg.scale(scaleFactor)
      resizedImg ← scaledImg.resizeTo(side, side, Position.Center)
    } yield resizedImg
  }

  def resizeToSmall(aimg: AsyncImage)(implicit ec: ExecutionContext): Future[AsyncImage] = resizeTo(aimg, 100)

  def resizeToLarge(aimg: AsyncImage)(implicit ec: ExecutionContext): Future[AsyncImage] = resizeTo(aimg, 200)

  def dimensions(aimg: AsyncImage)(implicit ec: ExecutionContext): (Int, Int) =
    (aimg.width, aimg.height)

  def scaleAvatar(
    fullFileId: Long,
    rnd:        ThreadLocalRandom,
    bucketName: String
  )(implicit transferManager: TransferManager, db: Database, ec: ExecutionContext, system: ActorSystem) = {
    /*val smallFileId = rnd.nextLong()
    val smallAccessSalt = ACL.nextAccessSalt(rnd)

    val largeFileId = rnd.nextLong()
    val largeAccessSalt = ACL.nextAccessSalt(rnd)*/

    persist.File.find(fullFileId) flatMap {
      case Some(fullFileModel) ⇒
        downloadFile(bucketName, fullFileId) flatMap {
          case Some(fullFile) ⇒
            val action = for {
              fullAimg ← DBIO.from(AsyncImage(fullFile))
              (fiw, fih) = dimensions(fullAimg)

              smallAimg ← DBIO.from(resizeToSmall(fullAimg))
              largeAimg ← DBIO.from(resizeToLarge(fullAimg))

              smallFile = fullFile.getParentFile.toPath.resolve("small.jpg").toFile
              largeFile = fullFile.getParentFile.toPath.resolve("large.jpg").toFile

              _ ← DBIO.from(smallAimg.writer(Format.JPEG).write(smallFile))
              _ ← DBIO.from(largeAimg.writer(Format.JPEG).write(largeFile))

              smallFileLocation ← uploadFile(bucketName, smallFile)
              largeFileLocation ← uploadFile(bucketName, largeFile)
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
                FileLocation(fullFileId, ACL.fileAccessHash(fullFileId, fullFileModel.accessSalt)),
                fullAimg.width,
                fullAimg.height,
                fullFile.length().toInt
              )

              Some(Avatar(Some(smallImage), Some(largeImage), Some(fullImage)))
            }

            action.asTry map {
              case Success(res) ⇒ res
              case Failure(e)   ⇒ None
            }
          case None ⇒ DBIO.successful(None)
        }
      case None ⇒
        DBIO.successful(None)
    }
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
