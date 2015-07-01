package im.actor.server.api.http

import akka.http.scaladsl.server.Route

trait RoutesHandler {
  def routes: Route
}