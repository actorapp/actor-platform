package im.actor.server.api.http

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ PathMatcher, _ }

object HttpApiHelpers extends HttpApiHelpers

trait HttpApiHelpers {
  val corsHeaders = List(
    headers.RawHeader("Access-Control-Allow-Origin", "*"),
    headers.RawHeader("Access-Control-Allow-Methods", "GET, POST, PUT"),
    headers.RawHeader("Access-Control-Allow-Credentials", "true"),
    headers.RawHeader("Access-Control-Allow-Headers", "DNT,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Accept,Range")
  )

  //todo: find better solution with Directive
  def defaultVersion(route: Route): Route = respondWithDefaultHeaders(corsHeaders) {
    pathPrefix("v1") {
      route
    }
  }

  val SignedLongNumber: PathMatcher1[Long] =
    PathMatcher("""[+-]?\d+""".r) flatMap { string ⇒
      try Some(java.lang.Long.parseLong(string))
      catch { case _: NumberFormatException ⇒ None }
    }

  trait ActorCustomRejection extends Rejection

}
