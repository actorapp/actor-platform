package im.actor.server.api.http.status

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import im.actor.server.api.http.RoutesHandler

class StatusHandler extends RoutesHandler {
  override def routes: Route = path("status") {
    get {
      complete("")
    }
  }
}