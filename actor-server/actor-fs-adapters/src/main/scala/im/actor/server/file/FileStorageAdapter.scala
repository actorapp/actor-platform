package im.actor.server.file

import java.io.File

import im.actor.server.model
import im.actor.server.db.ActorPostgresDriver.api._

import scala.concurrent._, duration._

object FileStorageAdapter {
  val UrlExpirationTimeout = 1.day
}

trait FileStorageAdapter extends UploadActions with DownloadActions with UploadKeyParsing

final case class UnsafeFileName(fileName: String) {
  lazy val safe: String = new File(fileName.replace("\u0000", "")).toPath.normalize().getFileName.toString
}

private[file] trait UploadActions {

  def getFileUploadPartUrl(fileId: Long, partNumber: Int): Future[(UploadKey, String)]

  def getFileUploadUrl(fileId: Long): Future[(UploadKey, String)]

  def completeFileUpload(fileId: Long, fieSize: Long, fileName: UnsafeFileName, partNames: Seq[String]): Future[Unit]

  def uploadFile(name: UnsafeFileName, file: File): DBIO[FileLocation]

  def uploadFileF(name: UnsafeFileName, file: File): Future[FileLocation]
}

private[file] trait UploadKeyParsing {
  def parseKey(bytes: Array[Byte]): UploadKey
}

private[file] trait DownloadActions {
  def getFileDownloadUrl(file: model.File, accessHash: Long): Future[Option[String]]

  def downloadFile(id: Long): DBIO[Option[File]]

  def downloadFileF(id: Long): Future[Option[File]]
}

