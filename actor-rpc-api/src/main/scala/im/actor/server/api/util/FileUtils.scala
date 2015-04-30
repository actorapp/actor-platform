package im.actor.server.api.util

import java.io.File

import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.{ ExecutionContext, Future, blocking }
import scalaz.\/

import akka.actor.ActorSystem
import com.amazonaws.services.s3.transfer.TransferManager
import com.amazonaws.services.s3.transfer.model.UploadResult
import com.github.dwhjames.awswrap.s3.FutureTransfer
import slick.dbio
import slick.dbio.Effect.{ Read, Write }
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.files.FileLocation
import im.actor.server.persist
import im.actor.server.util.ACLUtils

object FileUtils {
  // TODO: move file checks into FileUtils
  object Errors {
    val FileNotFound = RpcError(404, "FILE_NOT_FOUND", "File not found.", false, None)
    val FileTooLarge = RpcError(400, "FILE_TOO_LARGE", "File is too large.", false, None)
    val LocationInvalid = RpcError(400, "LOCATION_INVALID", "", false, None)
  }

  def downloadFile(bucketName: String, id: Long)(
    implicit
    transferManager: TransferManager,
    db:              Database,
    ec:              ExecutionContext
  ): dbio.DBIOAction[Option[File], NoStream, Read with Effect] = {
    persist.File.find(id) flatMap {
      case Some(file) ⇒
        download(bucketName, file.id) map (Some(_))
      case None ⇒ DBIO.successful(None)
    }
  }

  def download(bucketName: String, id: Long)(implicit transferManager: TransferManager, ec: ExecutionContext) = {
    for {
      dirFile ← DBIO.from(createTempDir())
      file = dirFile.toPath.resolve("file").toFile
      _ ← DBIO.from(FutureTransfer.listenFor(transferManager.download(bucketName, s3Key(id), file)) map (_.waitForCompletion()))
    } yield file
  }

  def uploadFile(bucketName: String, file: File)(
    implicit
    transferManager: TransferManager,
    ec:              ExecutionContext,
    system:          ActorSystem
  ): dbio.DBIOAction[FileLocation, NoStream, Write with Effect] = {
    val rnd = ThreadLocalRandom.current()
    val id = rnd.nextLong()
    val accessSalt = ACLUtils.nextAccessSalt(rnd)
    val sizeF = getFileLength(file)

    for {
      _ ← persist.File.create(id, accessSalt, s3Key(id))
      _ ← DBIO.from(upload(bucketName, id, file))
      _ ← DBIO.from(sizeF) flatMap (s ⇒ persist.File.setUploaded(id, s))
    } yield FileLocation(id, ACLUtils.fileAccessHash(id, accessSalt))
  }

  def upload(bucketName: String, id: Long, file: File)(
    implicit
    transferManager: TransferManager,
    ec:              ExecutionContext
  ): Future[UploadResult] = {
    FutureTransfer.listenFor(transferManager.upload(bucketName, s3Key(id), file)) map (_.waitForUploadResult())
  }

  def s3Key(id: Long): String = s"file_${id}"

  // FIXME: #perf pinned dispatcher
  def createTempDir()(implicit ec: ExecutionContext): Future[File] = {
    Future {
      blocking {
        com.google.common.io.Files.createTempDir()
      }
    }
  }

  def deleteDir(dir: File)(implicit ec: ExecutionContext): Future[Unit] = {
    Future {
      blocking {
        org.apache.commons.io.FileUtils.deleteDirectory(dir)
      }
    }
  }

  def getFileLength(file: File)(implicit ec: ExecutionContext): Future[Long] = {
    Future {
      blocking {
        file.length()
      }
    }
  }

  def withFileLocation[R <: RpcResponse](fileLocation: FileLocation, maxSize: Int)(f: ⇒ DBIO[RpcError \/ R])(implicit ec: ExecutionContext, s: ActorSystem) = {
    persist.File.find(fileLocation.fileId) flatMap {
      case Some(file) ⇒
        if (!file.isUploaded) {
          DBIO.successful(Error(Errors.LocationInvalid))
        } else if (file.size > maxSize) {
          DBIO.successful(Error(Errors.FileTooLarge))
        } else if (ACLUtils.fileAccessHash(file.id, file.accessSalt) != fileLocation.accessHash) {
          DBIO.successful(Error(Errors.LocationInvalid))
        } else {
          f
        }
      case None ⇒ DBIO.successful(Error(Errors.FileNotFound))
    }
  }
}