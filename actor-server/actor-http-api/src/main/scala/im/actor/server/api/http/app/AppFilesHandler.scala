package im.actor.server.api.http.app

import java.io.File
import java.nio.file.Paths

import scala.concurrent.ExecutionContext

import akka.http.scaladsl.model.StatusCodes.{ BadRequest, NotFound }
import akka.http.scaladsl.model.{ ContentTypes, HttpResponse }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._

import im.actor.server.api.http.RoutesHandler

class AppFilesHandler(staticFilesDirectory: String)(implicit ec: ExecutionContext) extends RoutesHandler {
  val rejection = RejectionHandler.newBuilder()
    .handle {
      case AuthorizationFailedRejection ⇒ complete(HttpResponse(BadRequest, entity = "You are not allowed to perform this action"))
    }
    .handleNotFound(complete(HttpResponse(NotFound, entity = "File not found")))
    .result()

  val base = Paths.get(staticFilesDirectory).toFile

  override def routes: Route = path("app" / Segment) { fileName ⇒
    get {
      handleRejections(rejection) {
        mapResponseEntity(_.withContentType(ContentTypes.`application/octet-stream`)) {
          validateFilePath(fileName) { file ⇒
            getFromFile(file)
          }
        }
      }
    }
  }

  def validateFilePath(path: String): Directive1[File] = {
    Directive { fileCompl ⇒
      val file = new File(base, path)
      if (file.getCanonicalPath.startsWith(base.getCanonicalPath))
        fileCompl(Tuple1(file))
      else
        reject(AuthorizationFailedRejection)
    }

  }
}
