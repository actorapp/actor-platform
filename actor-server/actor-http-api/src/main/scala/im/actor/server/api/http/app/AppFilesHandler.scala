package im.actor.server.api.http.app

import java.nio.file.Paths

import scala.concurrent.ExecutionContext

import akka.http.scaladsl.model.StatusCodes.{ BadRequest, NotFound }
import akka.http.scaladsl.model._
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

  override def routes: Route = pathPrefix("app") {
    getFromDirectory(staticFilesDirectory)
  }
}
