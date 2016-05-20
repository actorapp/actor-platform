package im.actor.server.file

import java.io.{ File, FileOutputStream }
import java.nio.file.{ Files, Path }

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{ FileIO, Source }
import akka.util.ByteString

import scala.concurrent.{ ExecutionContext, Future, blocking }
import scala.util.{ Failure, Success }

object FileUtils {

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
      ioRes ← Source.single(bytes).runWith(FileIO.toPath(file))
    } yield {
      ioRes.status match {
        case Success(_)     ⇒ (file, ioRes.count)
        case Failure(cause) ⇒ throw cause
      }
    }
  }

  def concatFiles(dir: File, fileNames: Seq[String])(implicit ec: ExecutionContext): Future[File] = {
    Future {
      blocking {
        val dirPath = dir.toPath
        val concatFile = dirPath.resolve("concatenated").toFile

        val outStream = new FileOutputStream(concatFile)

        fileNames foreach { fileName ⇒
          Files.copy(dirPath.resolve(fileName), outStream)
        }

        outStream.close()

        concatFile
      }
    }
  }
}