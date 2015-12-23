package im.actor.server.api.http

import akka.actor._
import akka.http.ServerSettings
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.Credentials
import akka.stream.{ ActorMaterializer, Materializer }
import com.typesafe.config.Config
import im.actor.server.api.http.app.AppFilesHandler
import im.actor.server.api.http.bots.BotsHandler
import im.actor.server.api.http.groups.GroupsHandler
import im.actor.server.api.http.status.StatusHandler
import im.actor.server.api.http.webhooks.WebhooksHandler
import im.actor.server.db.DbExtension
import im.actor.server.persist.HttpApiTokenRepo
import im.actor.tls.TlsContext

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

final class HttpApi(_system: ActorSystem) extends Extension {
  implicit val system = _system
  implicit val ec = system.dispatcher
  implicit val mat = ActorMaterializer()

  val hooks = new HttpApiHookControl

  HttpApiFrontend.start(system.settings.config)

  val authenticator: AsyncAuthenticator[Boolean] = {
    case p @ Credentials.Provided(_) ⇒
      for {
        tokens ← DbExtension(system).db.run(HttpApiTokenRepo.fetchAll)
      } yield tokens.find(t ⇒ p.verify(t.token)).map(_.isAdmin)
    case Credentials.Missing ⇒ Future.successful(None)
  }

  val adminAuthenticator: AsyncAuthenticator[Unit] =
    authenticator andThen (_ map (isAdminOpt ⇒ if (isAdminOpt.isDefined) Some(()) else None))
}

object HttpApi extends ExtensionId[HttpApi] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): HttpApi = new HttpApi(system)

  override def lookup(): ExtensionId[_ <: Extension] = HttpApi
}

private object HttpApiFrontend {
  import HttpApiHelpers._

  private val IdleTimeout = 15.minutes

  def start(serverConfig: Config)(
    implicit
    system:       ActorSystem,
    materializer: Materializer
  ): Unit = {
    HttpApiConfig.load(serverConfig.getConfig("http")) match {
      case Success(apiConfig) ⇒
        val tlsContext = TlsContext.load(serverConfig.getConfig("tls.keystores")).right.toOption
        start(apiConfig, tlsContext)
      case Failure(e) ⇒
        throw e
    }
  }

  def start(config: HttpApiConfig, tlsContext: Option[TlsContext])(implicit system: ActorSystem, materializer: Materializer): Unit = {
    import system.dispatcher

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

    // FIXME: consider more optimal Route creation

    def routesFuture: Future[Route] =
      for {
        custom ← customRoutes
      } yield custom.foldLeft(defaultRoutes)(_ ~ _)

    def customRoutes: Future[Seq[Route]] =
      HttpApi(system).hooks.routesHook.runAll()

    val defaultSettings = ServerSettings(system)

    Http().bind(
      config.interface,
      config.port,
      httpsContext = tlsContext map (_.asHttpsContext),
      settings = defaultSettings.copy(timeouts = defaultSettings.timeouts.copy(idleTimeout = IdleTimeout))
    )
      .mapAsync(1) { conn ⇒
        routesFuture map (conn → _)
      }
      .runForeach {
        case (conn, routes) ⇒
          conn handleWith routes
      }
  }
}
