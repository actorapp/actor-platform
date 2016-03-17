package im.actor.server.api.http

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ RejectionHandler, Route }
import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.settings.ServerSettings
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import im.actor.server.api.http.app.AppFilesHttpHandler
import im.actor.server.api.http.info.AboutHttpHandler
import im.actor.server.api.http.status.StatusHttpHandler
import im.actor.server.db.DbExtension
import im.actor.server.persist.HttpApiTokenRepo

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

final class HttpApi(_system: ActorSystem) extends Extension {
  implicit val system = _system
  implicit val ec = system.dispatcher
  implicit val mat = ActorMaterializer()

  private val hooks = new HttpApiHookControl

  def runRouteHooks(): Seq[Route] = hooks.routesHook.runAll()
  def runRejectionHooks(): Seq[RejectionHandler] = hooks.rejectionsHook.runAll()

  var customRejections: Seq[RejectionHandler] = runRejectionHooks()
  var customRoutes: Seq[Route] = runRouteHooks()

  def registerRoute(name: String)(f: ActorSystem ⇒ Route): Unit = {
    hooks.routesHook.register(s"$name-routes", new HttpApiHook.RoutesHook(system) {
      override def run(): Route = f(system)
    })

    synchronized {
      this.customRoutes = runRouteHooks()
    }
  }

  def registerRejection(name: String)(f: ActorSystem ⇒ RejectionHandler): Unit = {
    hooks.rejectionsHook.register(s"$name-rejection", new HttpApiHook.RejectionsHook(system) {
      def run(): RejectionHandler = f(system)
    })

    synchronized {
      this.customRejections = runRejectionHooks()
    }
  }

  val authenticator: AsyncAuthenticator[Boolean] = {
    case p @ Credentials.Provided(_) ⇒
      for {
        tokens ← DbExtension(system).db.run(HttpApiTokenRepo.fetchAll)
      } yield tokens.find(t ⇒ p.verify(t.token)).map(_.isAdmin)
    case Credentials.Missing ⇒ Future.successful(None)
  }

  val adminAuthenticator: AsyncAuthenticator[Unit] =
    authenticator andThen (_ map (isAdminOpt ⇒ if (isAdminOpt.isDefined) Some(()) else None))

  def start(): Unit =
    HttpApiFrontend.start(system.settings.config)
}

object HttpApi extends ExtensionId[HttpApi] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): HttpApi = new HttpApi(system)

  override def lookup(): ExtensionId[_ <: Extension] = HttpApi
}

private object HttpApiFrontend {
  import HttpApiHelpers._

  private val IdleTimeout = 15.minutes

  def start(serverConfig: Config)(implicit system: ActorSystem): Unit = {
    HttpApiConfig.load(serverConfig.getConfig("http")) match {
      case Success(apiConfig) ⇒
        start(apiConfig)
      case Failure(e) ⇒
        throw e
    }
  }

  def start(config: HttpApiConfig)(implicit system: ActorSystem): Unit = {
    implicit val mat = ActorMaterializer()

    val status = new StatusHttpHandler
    val info = new AboutHttpHandler
    val app = new AppFilesHttpHandler(config.staticFiles)

    def defaultRoutes: Route = app.routes ~ defaultVersion(status.routes ~ info.routes)

    def routes = HttpApi(system).customRoutes.foldLeft(defaultRoutes)(_ ~ _)
    def rejectionHandlers = HttpApi(system).customRejections.foldRight(RejectionHandler.default)((acc, el) ⇒ acc.withFallback(el))

    val defaultSettings = ServerSettings(system)

    Http().bind(
      config.interface,
      config.port,
      connectionContext = Http().defaultServerHttpContext,
      settings = defaultSettings.withTimeouts(defaultSettings.timeouts.withIdleTimeout(IdleTimeout))
    )
      .runForeach { conn ⇒
        conn handleWith handleRejections(rejectionHandlers) { routes }
      }
  }
}
