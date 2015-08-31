package im.actor.server.api.http.status

import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport

import im.actor.server.api.http.RoutesHandler
import im.actor.server.api.http.json.{ JsonFormatters, Status }

class StatusHandler extends RoutesHandler with PlayJsonSupport {

  import JsonFormatters._

  override def routes: Route = path("status") {
    get {
      complete(OK → Status("Ok"))
    }
  }
}