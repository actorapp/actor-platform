package im.actor.server.file.local

import java.nio.file.Path
import java.util.concurrent.Executors

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.util.FastFuture
import akka.stream.Materializer
import akka.stream.scaladsl.{ FileIO, Source }
import akka.util.ByteString
import better.files._
import im.actor.server.db.DbExtension
import im.actor.server.model.FilePart
import im.actor.server.persist.files.{ FilePartRepo, FileRepo }

import scala.concurrent.{ ExecutionContext, Future, blocking }
import scala.util.{ Failure, Success }

trait FileStorageOperations extends LocalUploadKeyImplicits {

  protected implicit val system: ActorSystem //just for logging
  protected implicit val ec: ExecutionContext
  protected implicit val mat: Materializer
  protected val storageLocation: String

  private lazy val poolSize = system.settings.config.getInt("services.file-storage.thread-pool-size")
  private lazy val ecPool = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(poolSize))

  private lazy val log = Logging(system, getClass)
  private lazy val db = DbExtension(system).db

  protected def createFile(fileId: Long, name: String, data: Array[Byte]): Future[Unit] = {
    for {
      dir ← getOrCreateFileDir(fileId)
      file = dir / name
      _ ← Future { blocking { file.createIfNotExists() } }
      ioRes ← Source(List(ByteString(data))).runWith(FileIO.toPath(file.path))
    } yield ioRes.status match {
      case Success(_)     ⇒ ()
      case Failure(cause) ⇒ throw cause
    }
  }

  protected def prepareForPartWrite(fileId: Long, partNumber: Int): Future[Unit] = {
    val partUploadKey = LocalUploadKey.partKey(fileId, partNumber).key

    for {
      dir ← getOrCreateFileDir(fileId)
      partFile = dir / partUploadKey
      _ ← if (partFile.exists) Future { blocking { partFile.delete(ignoreIOExceptions = true) } } else FastFuture.successful(())
    } yield ()
  }

  protected def appendPartBytes(bs: Source[ByteString, Any], fileId: Long, partNumber: Int): Future[Unit] = {
    for {
      dir ← getOrCreateFileDir(fileId)
      partFile ← Future { blocking { dir.createChild(LocalUploadKey.partKey(fileId, partNumber).key) } }
      _ = log.debug("Appending bytes to part number: {}, fileId: {}, target file: {}", partNumber, fileId, partFile)
      _ ← bs.runWith(FileIO.toPath(partFile.path))
    } yield ()
  }

  protected def haveAllParts(dir: File, partNames: Seq[String], fileSize: Long): Future[Boolean] = Future {
    val partsSize = (partNames map { name ⇒ (dir / name).size }).sum
    val result = partsSize == fileSize
    if (!result) {
      log.debug(
        "Failed to concat file, some parts are not there yet. Expected file size: {}, sum of parts size: {}",
        fileSize,
        partsSize
      )
    }
    result
  }

  //TODO Delete uploaded file parts. ??
  /*protected def deleteUploadedParts(dir: File, partNames: Seq[String]): Future[Unit] =
      Future.sequence(partNames map { part ⇒ Future((dir / part).delete()) }) map (_ ⇒ ())*/

  protected def getFileData(fileId: Long): Future[Option[Source[ByteString, Any]]] =
    for {
      parts ← db.run(FilePartRepo.findByFileId(fileId))
      file ← getFile(fileId)
      data = file.filter(_.isUploaded).map { f ⇒
        fileStream((fileDirectory(fileId) / f.name).path).getOrElse {
          filePartsStream(parts)
        }
      }
    } yield data

  protected def getFile(fileId: Long): Future[Option[im.actor.server.model.File]] =
    db.run(FileRepo.find(fileId))

  private def fileStream(path: Path): Option[Source[ByteString, Any]] =
    if (path.exists) Some(FileIO.fromPath(path)) else None

  protected def filePartsStream(parts: Seq[FilePart]): Source[ByteString, Any] = {
    val paths: List[Path] = parts.map(partFilePath)(collection.breakOut)

    Source.unfold[List[Path], Source[ByteString, Any]](paths) { xs ⇒
      xs.headOption.map { path ⇒
        xs.tail → FileIO.fromPath(path)
      }
    }.flatMapConcat(identity)
  }

  protected def fileDirectory(fileId: Long): File = file"$storageLocation/file_$fileId"

  private def partFilePath(filePart: FilePart): Path = (fileDirectory(filePart.fileId) / filePart.uploadKey).path

  private def getOrCreateFileDir(fileId: Long) = Future {
    blocking {
      fileDirectory(fileId).createIfNotExists(asDirectory = true)
    }
  }

}