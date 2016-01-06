package im.actor.server.api.http.app

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import im.actor.server.api.http.HttpHandler

import scala.concurrent.ExecutionContext

private[http] final class AppFilesHttpHandler(staticFilesDirectory: String) extends HttpHandler {
  override def routes: Route = pathPrefix("app") {
    getFromDirectory(staticFilesDirectory)
  }
}
