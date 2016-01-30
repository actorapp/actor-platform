package im.actor.server.api.http

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

object HttpApiHelpers extends HttpApiHelpers

trait HttpApiHelpers {
  val corsHeaders = List(
    headers.RawHeader("Access-Control-Allow-Origin", "*"),
    headers.RawHeader("Access-Control-Allow-Methods", "GET, POST, PUT"),
    headers.RawHeader("Access-Control-Allow-Headers", "*"),
    headers.RawHeader("Access-Control-Allow-Credentials", "true")
  )

  //todo: find better solution with Directive
  def defaultVersion(route: Route): Route = respondWithDefaultHeaders(corsHeaders) {
    pathPrefix("v1") {
      route
    }
  }

}
