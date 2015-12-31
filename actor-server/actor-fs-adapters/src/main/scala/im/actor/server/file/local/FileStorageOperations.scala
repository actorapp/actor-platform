package im.actor.server.file.local

import akka.actor.ActorSystem
import akka.event.Logging
import better.files.{ File, _ }

import scala.concurrent.{ ExecutionContext, Future, blocking }

trait FileStorageOperations extends LocalUploadKeyImplicits {

  protected implicit val system: ActorSystem //just for logging
  protected implicit val ec: ExecutionContext
  protected val storageLocation: String

  private lazy val log = Logging(system, getClass)

  protected def createFile(fileId: Long, name: String, file: File): Future[File] =
    Future(file.copyTo(getOrCreateFileDir(fileId) / getFileName(name)))

  protected def prepareForPartWrite(fileId: Long, partNumber: Int): Future[Unit] = Future {
    val partUploadKey = LocalUploadKey.partKey(fileId, partNumber).key
    val partFile = getOrCreateFileDir(fileId) / partUploadKey
    if (partFile.exists) partFile.delete(ignoreIOExceptions = true)
    ()
  }

  protected def appendPartBytes(bytes: Array[Byte], fileId: Long, partNumber: Int): Future[Unit] = Future {
    val partFile = getOrCreateFileDir(fileId).createChild(LocalUploadKey.partKey(fileId, partNumber).key)
    log.debug("Appending bytes to part number: {}, fileId: {}, data length: {} target file: {}", partNumber, fileId, bytes.length, partFile)
    partFile append bytes
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

  protected def concatFiles(dir: File, partNames: Seq[String], fileName: String, fileSize: Long): Future[File] = {
    Future {
      blocking {
        log.debug("Concatenating file: {}, parts number: {}", fileName, partNames.length)
        val concatFile = dir.createChild(getFileName(fileName))
        for {
          out ← concatFile.outputStream
          _ ← for {
            iss ← partNames map { name ⇒
              log.debug("Concatenating part: {}", name)
              (dir / name).inputStream
            }
          } yield iss.foreach(_.pipeTo(out, closeOutputStream = false))
        } yield ()
        concatFile
      }
    }
  }

  protected def deleteUploadedParts(dir: File, partNames: Seq[String]): Future[Unit] =
    Future.sequence(partNames map { part ⇒ Future((dir / part).delete()) }) map (_ ⇒ ())

  protected def getFile(fileId: Long, optName: Option[String]): Future[File] =
    getFile(fileId, optName getOrElse "")

  protected def getFile(fileId: Long, name: String): Future[File] =
    Future(fileDirectory(fileId) / getFileName(name))

  protected def getFileName(name: String) = if (name.trim.isEmpty) "file" else name

  protected  def fileDirectory(fileId: Long): File = file"$storageLocation/file_${fileId}"

  private def getOrCreateFileDir(fileId: Long) = fileDirectory(fileId).createIfNotExists(asDirectory = true)

}
