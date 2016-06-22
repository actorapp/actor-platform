package im.actor.server.file

import java.io.File

import akka.actor.ActorSystem
import akka.stream.{ ActorMaterializer, Materializer }
import akka.stream.scaladsl.Source
import akka.util.ByteString
import im.actor.server.model.{ File ⇒ FileModel }
import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.util.misc.StringUtils.{ isAsciiString, toAsciiString, transliterate }

import scala.concurrent._
import duration._

object FileStorageAdapter {
  val UrlExpirationTimeout = 1.day
}

trait FileStorageAdapter extends UploadActions with DownloadActions with UploadKeyParsing

final case class UnsafeFileName(fileName: String) {
  lazy val safe: String = {
    val name = fileName.replace("\u0000", "")
    val validName = if (isAsciiString(name)) name else toAsciiString(transliterate(name))
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

  def downloadFile(id: Long): DBIO[Option[Source[ByteString, Any]]]

  def downloadFileF(id: Long): Future[Option[Source[ByteString, Any]]]

  def downloadAsArray(id: Long)(implicit ec: ExecutionContext, as: ActorSystem): Future[Option[Array[Byte]]] = downloadFileF(id).flatMap { opt ⇒
    val promise = Promise[Option[Array[Byte]]]
    implicit val mat = ActorMaterializer()

    opt match {
      case Some(source) ⇒
        val bs = source.runFold(ByteString.empty)(_ ++ _).map(_.toArray)
        bs.onComplete(x ⇒ promise.tryComplete(x.map(Some.apply)))
      case None ⇒ promise.trySuccess(None)
    }
    promise.future
  }
}

