package im.actor.server.util

import java.io.File
import java.nio.file.{ Files, Path }

import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.{ ExecutionContext, Future, blocking }

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.io.SynchronousFileSink
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.amazonaws.services.s3.transfer.TransferManager
import com.amazonaws.services.s3.transfer.model.UploadResult
import com.github.dwhjames.awswrap.s3.FutureTransfer
import slick.dbio
import slick.dbio.Effect.{ Read, Write }
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.files.FileLocation
import im.actor.server.persist

object FileUtils {

  def downloadFile(bucketName: String, id: Long)(
    implicit
    transferManager: TransferManager,
    db:              Database,
    ec:              ExecutionContext
  ): dbio.DBIOAction[Option[File], NoStream, Read with Effect] = {
    persist.File.find(id) flatMap {
      case Some(file) ⇒
        download(bucketName, file.id, file.name) map (Some(_))
      case None ⇒ DBIO.successful(None)
    }
  }

  def download(bucketName: String, id: Long, name: String)(implicit transferManager: TransferManager, ec: ExecutionContext) = {
    for {
      dirFile ← DBIO.from(createTempDir())
      file = dirFile.toPath.resolve("file").toFile
      _ ← DBIO.from(FutureTransfer.listenFor(transferManager.download(bucketName, s3Key(id, name), file)) map (_.waitForCompletion()))
    } yield file
  }

  def uploadFile(bucketName: String, name: String, file: File)(
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
      _ ← persist.File.create(id, accessSalt, s3Key(id, name))
      _ ← DBIO.from(upload(bucketName, id, name, file))
      _ ← DBIO.from(sizeF) flatMap (s ⇒ persist.File.setUploaded(id, s, name))
    } yield FileLocation(id, ACLUtils.fileAccessHash(id, accessSalt))
  }

  def upload(bucketName: String, id: Long, name: String, file: File)(
    implicit
    transferManager: TransferManager,
    ec:              ExecutionContext
  ): Future[UploadResult] = {
    FutureTransfer.listenFor(transferManager.upload(bucketName, s3Key(id, name), file)) map (_.waitForUploadResult())
  }

  def s3Key(id: Long, name: String): String = {
    if (name.isEmpty) {
      s"file_${id}"
    } else {
      s"file_${id}/${name}"
    }
  }

  // FIXME: #perf pinned dispatcher
  def createTempDir()(implicit ec: ExecutionContext): Future[File] = {
    Future {
      blocking {
        Files.createTempDirectory("file-utils").toFile
      }
    }
  }

  def createTempFile(implicit ec: ExecutionContext): Future[Path] = {
    Future {
      blocking {
        Files.createTempFile("file", "")
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

  def writeBytes(bytes: ByteString)(implicit system: ActorSystem, materializer: FlowMaterializer, ec: ExecutionContext): Future[(Path, Long)] = {
    for {
      file ← createTempFile
      size ← Source.single(bytes).runWith(SynchronousFileSink(file.toFile))
    } yield (file, size)
  }

}