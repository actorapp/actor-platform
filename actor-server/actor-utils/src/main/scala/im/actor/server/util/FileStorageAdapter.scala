package im.actor.server.util

import java.io.File

import scala.concurrent._

import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.files.FileLocation
import im.actor.server.models

trait FileStorageAdapter extends UploadActions with DownloadActions

private[util] trait UploadActions {
  def uploadFile(name: String, file: File): DBIO[FileLocation]

  def uploadFileF(name: String, file: File): Future[FileLocation]
}

private[util] trait DownloadActions {
  def getFileUrl(file: models.File, accessHash: Long): Future[Option[String]]

  def downloadFile(id: Long): DBIO[Option[File]]

  def downloadFileF(id: Long): Future[Option[File]]
}

