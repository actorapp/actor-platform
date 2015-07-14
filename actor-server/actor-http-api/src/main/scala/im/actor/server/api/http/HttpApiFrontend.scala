package im.actor.server.api.http

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.typesafe.config.Config
import slick.driver.PostgresDriver.api._

import im.actor.server.api.http.app.AppFilesHandler
import im.actor.server.api.http.dashboard.DashboardHandler
import im.actor.server.api.http.groups.GroupsHandler
import im.actor.server.api.http.status.StatusHandler
import im.actor.server.api.http.webhooks.WebhooksHandler
import im.actor.server.email._
import im.actor.server.peermanagers.GroupPeerManagerRegion
import im.actor.server.tls.TlsContext
import im.actor.server.util.FileStorageAdapter

object HttpApiFrontend {
  private val corsHeaders = List(
    `Access-Control-Allow-Origin`.`*`,
    `Access-Control-Allow-Methods`(GET, POST),
    `Access-Control-Allow-Headers`("*"),
    `Access-Control-Allow-Credentials`(true)
  )

  def start(serverConfig: Config)(
    implicit
    system:                 ActorSystem,
    materializer:           Materializer,
    db:                     Database,
    groupPeerManagerRegion: GroupPeerManagerRegion,
    fsAdapter:              FileStorageAdapter,
    emailSender:            EmailSender
  ): Unit = {
    HttpApiConfig.load(serverConfig.getConfig("webapp")) match {
      case Success(apiConfig) ⇒
        val tlsContext = TlsContext.load(serverConfig.getConfig("tls.keystores")).right.toOption
        start(apiConfig, tlsContext)
      case Failure(e) ⇒
        throw e
    }
  }

  def start(config: HttpApiConfig, tlsContext: Option[TlsContext])(
    implicit
    system:                 ActorSystem,
    materializer:           Materializer,
    db:                     Database,
    groupPeerManagerRegion: GroupPeerManagerRegion,
    fsAdapter:              FileStorageAdapter,
    emailSender:            EmailSender
  ): Unit = {

    implicit val ec: ExecutionContext = system.dispatcher

    val webhooks = new WebhooksHandler
    val groups = new GroupsHandler
    val status = new StatusHandler
    val files = new AppFilesHandler(config.staticFiles)
    val dashboard = new DashboardHandler

    //format: OFF
    def routes: Route =
      files.routes ~
      dashboard.routes ~
      pathPrefix("v1") {
        respondWithDefaultHeaders(corsHeaders) {
          status.routes ~
          groups.routes ~
          webhooks.routes
        }
      }
    //format: ON

    Http().bind(config.interface, config.port, httpsContext = tlsContext map (_.asHttpsContext)).runForeach { connection ⇒
      connection handleWith Route.handlerFlow(routes)
    }
  }

}
