package im.actor.server.webhooks.http.routes

import akka.http.scaladsl.model.StatusCodes.{ Gone, OK }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.Segment
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import im.actor.server.api.http.json.{ Errors, JsonFormatters, Status }

trait TokenStatus extends PlayJsonSupport {
  self: WebhooksHttpHandler ⇒

  import JsonFormatters._

  def status: Route = {
    path(Segment / "status") { token ⇒
      onSuccess(integrationTokensKv.get(token)) {
        case Some(_) ⇒ complete(OK → Status("Ok"))
        case None    ⇒ complete(Gone → Errors("This token no longer exists(or never existed)"))
      }
    }
  }

}
