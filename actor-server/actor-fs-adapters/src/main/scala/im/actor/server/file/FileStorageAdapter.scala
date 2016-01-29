package im.actor.server.file

import java.io.File

import im.actor.server.model.{ File ⇒ FileModel }
import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.util.misc.StringUtils.{ isAsciiString, transliterate, toAsciiString }

import scala.concurrent._, duration._

object FileStorageAdapter {
  val UrlExpirationTimeout = 1.day
}

trait FileStorageAdapter extends UploadActions with DownloadActions with UploadKeyParsing

final case class UnsafeFileName(fileName: String) {
  lazy val safe: String = {
    val noWhitespace = fileName.replace("\u0000", "").replaceAll("\\s+", "_")
    val validName = if (isAsciiString(noWhitespace)) noWhitespace else toAsciiString(transliterate(noWhitespace))
    new File(validName).toPath.normalize().getFileName.toString
  }
}

private[file] trait UploadActions {

  def getFileUploadPartUrl(fileId: Long, partNumber: Int): Future[(UploadKey, String)]

  def getFileUploadUrl(fileId: Long): Future[(UploadKey, String)]

  def completeFileUpload(fileId: Long, fieSize: Long, fileName: UnsafeFileName, partNames: Seq[String]): Future[Unit]

  def uploadFile(name: UnsafeFileName, data: Array[Byte]): DBIO[FileLocation]

  def uploadFileF(name: UnsafeFileName, data: Array[Byte]): Future[FileLocation]
}

private[file] trait UploadKeyParsing {
  def parseKey(bytes: Array[Byte]): UploadKey
}

private[file] trait DownloadActions {
  def getFileDownloadUrl(file: FileModel, accessHash: Long): Future[Option[String]]

  def downloadFile(id: Long): DBIO[Option[Array[Byte]]]

  def downloadFileF(id: Long): Future[Option[Array[Byte]]]
}

