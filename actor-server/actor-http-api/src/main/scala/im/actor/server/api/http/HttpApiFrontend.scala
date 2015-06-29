package im.actor.server.api.http

import scala.concurrent.ExecutionContext

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.github.dwhjames.awswrap.s3.AmazonS3ScalaClient
import slick.driver.PostgresDriver.api._

import im.actor.server.api.http.files.FilesHandler
import im.actor.server.api.http.groups.GroupsHandler
import im.actor.server.api.http.status.StatusHandler
import im.actor.server.api.http.webhooks.WebhooksHandler
import im.actor.server.peermanagers.GroupPeerManagerRegion

object HttpApiFrontend {

  val corsHeaders = List(
    `Access-Control-Allow-Origin`.`*`,
    `Access-Control-Allow-Methods`(GET, POST),
    `Access-Control-Allow-Headers`("*"),
    `Access-Control-Allow-Credentials`(true)
  )

  def start(config: HttpApiConfig, s3BucketName: String)(
    implicit
    system:                 ActorSystem,
    materializer:           Materializer,
    db:                     Database,
    groupPeerManagerRegion: GroupPeerManagerRegion,
    client:                 AmazonS3ScalaClient
  ): Unit = {

    implicit val ec: ExecutionContext = system.dispatcher

    val webhooks = new WebhooksHandler
    val groups = new GroupsHandler(s3BucketName)
    val status = new StatusHandler
    val files = new FilesHandler(config.staticFilesDirectory)

    def routes: Route = pathPrefix("v1") {
      respondWithDefaultHeaders(corsHeaders) {
        status.routes ~
          groups.routes ~
          webhooks.routes ~
          files.routes
      }
    }

    Http().bind(config.interface, config.port).runForeach { connection ⇒
      connection handleWith Route.handlerFlow(routes)
    }
  }

}
