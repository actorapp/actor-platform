package im.actor.server.api.http.status

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

class StatusHandler {
  def routes: Route = path("status") {
    get {
      complete("")
    }
  }
}