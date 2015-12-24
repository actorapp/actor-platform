package im.actor.server.api.http

import akka.http.scaladsl.server.Route

trait HttpHandler extends HttpApiHelpers {
  def routes: Route
}