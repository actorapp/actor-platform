package im.actor.server.api.http

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers.{ `Access-Control-Allow-Credentials`, `Access-Control-Allow-Headers`, `Access-Control-Allow-Methods`, `Access-Control-Allow-Origin` }

object HttpApiHelpers {
  val corsHeaders = List(
    `Access-Control-Allow-Origin`.`*`,
    `Access-Control-Allow-Methods`(GET, POST, PUT),
    `Access-Control-Allow-Headers`("Content-Type", "Access-Control-Allow-Headers", "Authorization"),
    `Access-Control-Allow-Credentials`(true)
  )
}
