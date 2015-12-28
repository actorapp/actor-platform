package im.actor.server.api.http

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers.{ `Access-Control-Allow-Credentials`, `Access-Control-Allow-Headers`, `Access-Control-Allow-Methods`, `Access-Control-Allow-Origin` }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

object HttpApiHelpers extends HttpApiHelpers

trait HttpApiHelpers {
  val corsHeaders = List(
    `Access-Control-Allow-Origin`.`*`,
    `Access-Control-Allow-Methods`(GET, POST, PUT),
    `Access-Control-Allow-Headers`("Content-Type", "Access-Control-Allow-Headers", "Authorization"),
    `Access-Control-Allow-Credentials`(true)
  )

  //todo: find better solution with Directive
  def defaultVersion(route: Route): Route = respondWithDefaultHeaders(corsHeaders) {
    pathPrefix("v1") {
      route
    }
  }

}
