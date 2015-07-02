package im.actor.server.util

import java.io.File

import scala.concurrent._

import akka.actor.ActorSystem
import com.amazonaws.services.s3.transfer.TransferManager
import com.github.dwhjames.awswrap.s3.AmazonS3ScalaClient
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.files.FileLocation
import im.actor.server.models

trait FileStorageAdapter extends UploadActions with DownloadActions

private[util] trait UploadActions {
  def uploadFile(name: String, file: File): DBIO[FileLocation]

  def uploadFileF(name: String, file: File): Future[FileLocation]
}

private[util] trait DownloadActions {
  def getFileUrl(file: models.File, accessHash: Long, bucketName: String): Future[Option[String]]

  def downloadFile(id: Long): DBIO[Option[File]]

  def downloadFileF(id: Long): Future[Option[File]]
}

object FileStorageAdapter {
  /**
   * Constructs default UploadManager (S3)
   *
   * @param bucketName
   * @return S3 Upload Manager
   */
  def apply(bucketName: String)(
    implicit
    db:              Database,
    system:          ActorSystem,
    transferManager: TransferManager,
    s3Client:        AmazonS3ScalaClient
  ): FileStorageAdapter = new S3StorageAdapter(bucketName)
}

