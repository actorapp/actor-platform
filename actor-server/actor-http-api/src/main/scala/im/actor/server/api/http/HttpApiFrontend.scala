package im.actor.server.api.http

import akka.actor.ActorSystem
import akka.http.ServerSettings
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.typesafe.config.Config
import im.actor.server.api.http.app.AppFilesHandler
import im.actor.server.api.http.bots.BotsHandler
import im.actor.server.api.http.groups.GroupsHandler
import im.actor.server.api.http.status.StatusHandler
import im.actor.server.api.http.webhooks.WebhooksHandler
import im.actor.server.db.DbExtension
import im.actor.server.file.{ FileStorageAdapter, S3StorageExtension }
import im.actor.server.group.{ GroupExtension, GroupViewRegion }
import im.actor.tls.TlsContext
import slick.driver.PostgresDriver.api._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

object HttpApiFrontend {
  private val corsHeaders = List(
    `Access-Control-Allow-Origin`.`*`,
    `Access-Control-Allow-Methods`(GET, POST),
    `Access-Control-Allow-Headers`("*"),
    `Access-Control-Allow-Credentials`(true)
  )

  private val IdleTimeout = 15.minutes

  def start(serverConfig: Config, customRoutes: Seq[Route] = Seq.empty)(
    implicit
    system:       ActorSystem,
    materializer: Materializer
  ): Unit = {
    HttpApiConfig.load(serverConfig.getConfig("http")) match {
      case Success(apiConfig) ⇒
        val tlsContext = TlsContext.load(serverConfig.getConfig("tls.keystores")).right.toOption
        start(apiConfig, customRoutes, tlsContext)
      case Failure(e) ⇒
        throw e
    }
  }

  def start(config: HttpApiConfig, customRoutes: Seq[Route], tlsContext: Option[TlsContext])(
    implicit
    system:       ActorSystem,
    materializer: Materializer
  ): Unit = {

    implicit val ec: ExecutionContext = system.dispatcher
    implicit val db: Database = DbExtension(system).db
    implicit val groupProcessorRegion: GroupViewRegion = GroupExtension(system).viewRegion
    implicit val fileStorageAdapter: FileStorageAdapter = S3StorageExtension(system).s3StorageAdapter

    val webhooks = new WebhooksHandler
    val groups = new GroupsHandler
    val status = new StatusHandler
    val bots = new BotsHandler
    val app = new AppFilesHandler(config.staticFiles)

    // format: OFF
    def defaultRoutes: Route =
      app.routes ~
      pathPrefix("v1") {
        respondWithDefaultHeaders(corsHeaders) {
          bots.routes ~
          status.routes ~
          groups.routes ~
          webhooks.routes
        }
      }
    // format: ON

    def routes: Route = customRoutes.foldLeft(defaultRoutes)(_ ~ _)

    val defaultSettings = ServerSettings(system)

    Http().bind(
      config.interface,
      config.port,
      httpsContext = tlsContext map (_.asHttpsContext),
      settings = defaultSettings.copy(timeouts = defaultSettings.timeouts.copy(idleTimeout = IdleTimeout))
    )
      .runForeach { connection ⇒
        connection handleWith Route.handlerFlow(routes)
      }
  }

}
