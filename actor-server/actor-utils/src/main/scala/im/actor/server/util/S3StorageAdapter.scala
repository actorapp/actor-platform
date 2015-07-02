package im.actor.server.util

import java.io.File

import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.ActorSystem
import com.amazonaws.HttpMethod
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.transfer.TransferManager
import com.amazonaws.services.s3.transfer.model.UploadResult
import com.github.dwhjames.awswrap.s3.{ AmazonS3ScalaClient, FutureTransfer }
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.files.FileLocation
import im.actor.server.{ models, persist }

class S3StorageAdapter(bucketName: String)(
  implicit
  db:              Database,
  system:          ActorSystem,
  transferManager: TransferManager,
  s3Client:        AmazonS3ScalaClient
) extends FileStorageAdapter {
  private implicit val ec: ExecutionContext = system.dispatcher

  override def uploadFile(name: String, file: File): DBIO[FileLocation] =
    uploadFile(bucketName, name, file)

  override def uploadFileF(name: String, file: File): Future[FileLocation] =
    db.run(uploadFile(name, file))

  override def downloadFile(id: Long): DBIO[Option[File]] = {
    persist.File.find(id) flatMap {
      case Some(file) ⇒
        downloadFile(bucketName, file.id, file.name) map (Some(_))
      case None ⇒ DBIO.successful(None)
    }
  }

  override def downloadFileF(id: Long): Future[Option[File]] =
    db.run(downloadFile(id))

  override def getFileUrl(file: models.File, accessHash: Long, bucketName: String): Future[Option[String]] = {
    val timeout = 1.day

    if (ACLUtils.fileAccessHash(file.id, file.accessSalt) == accessHash) {
      val presignedRequest = new GeneratePresignedUrlRequest(bucketName, FileUtils.s3Key(file.id, file.name))

      val expiration = new java.util.Date
      expiration.setTime(expiration.getTime + timeout.toMillis)
      presignedRequest.setExpiration(expiration)
      presignedRequest.setMethod(HttpMethod.GET)

      s3Client.generatePresignedUrlRequest(presignedRequest).map(_.toString).map(Some(_))
    } else Future.successful(None)
  }

  private def downloadFile(bucketName: String, id: Long, name: String) = {
    for {
      dirFile ← DBIO.from(FileUtils.createTempDir())
      file = dirFile.toPath.resolve("file").toFile
      _ ← DBIO.from(FutureTransfer.listenFor(transferManager.download(bucketName, FileUtils.s3Key(id, name), file)) map (_.waitForCompletion()))
    } yield file
  }

  private def uploadFile(bucketName: String, name: String, file: File): DBIO[FileLocation] = {
    val rnd = ThreadLocalRandom.current()
    val id = rnd.nextLong()
    val accessSalt = ACLUtils.nextAccessSalt(rnd)
    val sizeF = FileUtils.getFileLength(file)

    for {
      _ ← persist.File.create(id, accessSalt, FileUtils.s3Key(id, name))
      _ ← DBIO.from(s3Upload(bucketName, id, name, file))
      _ ← DBIO.from(sizeF) flatMap (s ⇒ persist.File.setUploaded(id, s, name))
    } yield FileLocation(id, ACLUtils.fileAccessHash(id, accessSalt))
  }

  private def s3Upload(bucketName: String, id: Long, name: String, file: File): Future[UploadResult] = {
    FutureTransfer.listenFor(transferManager.upload(bucketName, FileUtils.s3Key(id, name), file)) map (_.waitForUploadResult())
  }
}