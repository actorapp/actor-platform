package im.actor.util.http

import java.nio.file.{ Files, Path }

import akka.stream.scaladsl.FileIO

import scala.concurrent._

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.Materializer

import scala.util.{ Success, Failure }

class DownloadManager(implicit system: ActorSystem, materializer: Materializer) {
  implicit val ec: ExecutionContext = system.dispatcher

  val http = Http()

  def download(url: String): Future[(Path, Long)] = {
    val tempFileFuture = createTempFile()
    val responseFuture = http.singleRequest(HttpRequest(uri = url))

    for {
      filePath ← tempFileFuture
      response ← responseFuture
      ioRes ← response.entity.dataBytes.runWith(FileIO.toPath(filePath))
    } yield {
      ioRes.status match {
        case Success(_)     ⇒ (filePath, ioRes.count)
        case Failure(cause) ⇒ throw cause
      }
    }
  }

  // FIXME: dispatcher for this
  private def createTempFile(): Future[Path] = {
    Future {
      blocking {
        Files.createTempFile("ActorDownloadManager", "")
      }
    }
  }
}