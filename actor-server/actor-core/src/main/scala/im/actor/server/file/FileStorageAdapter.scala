package im.actor.server.file

import java.io.File

import im.actor.api.rpc.files.ApiFileLocation
import im.actor.server.models
import slick.driver.PostgresDriver.api._

import scala.concurrent._

trait FileStorageAdapter extends UploadActions with DownloadActions

private[file] trait UploadActions {
  def uploadFile(name: String, file: File): DBIO[ApiFileLocation]

  def uploadFileF(name: String, file: File): Future[ApiFileLocation]
}

private[file] trait DownloadActions {
  def getFileUrl(file: models.File, accessHash: Long): Future[Option[String]]

  def downloadFile(id: Long): DBIO[Option[File]]

  def downloadFileF(id: Long): Future[Option[File]]
}

