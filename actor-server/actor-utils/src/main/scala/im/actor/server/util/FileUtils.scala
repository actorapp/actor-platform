package im.actor.server.util

import java.io.File
import java.nio.file.{ Files, Path }

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ ExecutionContext, Future, blocking }

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.io.SynchronousFileSink
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.amazonaws.HttpMethod
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.transfer.TransferManager
import com.github.dwhjames.awswrap.s3.{ AmazonS3ScalaClient, FutureTransfer }
import slick.dbio
import slick.dbio.Effect.Read
import slick.driver.PostgresDriver.api._

import im.actor.server.{ models, persist }

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

  def getFileUrl(file: models.File, accessHash: Long, bucketName: String)(
    implicit
    system:   ActorSystem,
    ec:       ExecutionContext,
    s3Client: AmazonS3ScalaClient,
    timeout:  FiniteDuration
  ): Future[Option[String]] = {
    if (ACLUtils.fileAccessHash(file.id, file.accessSalt) == accessHash) {
      val presignedRequest = new GeneratePresignedUrlRequest(bucketName, s3Key(file.id, file.name))

      val expiration = new java.util.Date
      expiration.setTime(expiration.getTime + timeout.toMillis)
      presignedRequest.setExpiration(expiration)
      presignedRequest.setMethod(HttpMethod.GET)

      s3Client.generatePresignedUrlRequest(presignedRequest).map(_.toString).map(Some(_))
    } else Future.successful(None)
  }

  // FIXME: #perf pinned dispatcher
  def getFileLength(file: File)(implicit ec: ExecutionContext): Future[Long] = {
    Future {
      blocking {
        file.length()
      }
    }
  }

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

  def writeBytes(bytes: ByteString)(implicit system: ActorSystem, materializer: Materializer, ec: ExecutionContext): Future[(Path, Long)] = {
    for {
      file ← createTempFile
      size ← Source.single(bytes).runWith(SynchronousFileSink(file.toFile))
    } yield (file, size)
  }

  def s3Key(id: Long, name: String): String = {
    if (name.isEmpty) {
      s"file_${id}"
    } else {
      s"file_${id}/${name}"
    }
  }
}