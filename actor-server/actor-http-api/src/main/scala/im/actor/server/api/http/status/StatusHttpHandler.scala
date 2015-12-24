package im.actor.server.api.http.status

import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport

import im.actor.server.api.http.HttpHandler
import im.actor.server.api.http.json.{ JsonFormatters, Status }

private[http] final class StatusHttpHandler extends HttpHandler with PlayJsonSupport {

  import JsonFormatters._

  override def routes: Route = path("status") {
    get {
      complete(OK → Status("Ok"))
    }
  }
}