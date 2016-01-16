package im.actor.server.api.http

import akka.actor._
import akka.http.ServerSettings
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.Credentials
import akka.stream.{ ActorMaterializerSettings, ActorMaterializer, Materializer }
import com.typesafe.config.Config
import im.actor.server.api.http.app.AppFilesHttpHandler
import im.actor.server.api.http.status.StatusHttpHandler
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

  private val hooks = new HttpApiHookControl

  def runHooks(): Seq[Route] = hooks.routesHook.runAll()

  var customRoutes: Seq[Route] = runHooks()

  def registerHook(name: String)(f: ActorSystem ⇒ Route): Unit = {
    hooks.routesHook.register(name, new HttpApiHook.RoutesHook(system) {
      override def run(): Route = f(system)
    })

    synchronized {
      this.customRoutes = runHooks()
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

  def start(serverConfig: Config)(
    implicit
    system: ActorSystem
  ): Unit = {
    HttpApiConfig.load(serverConfig.getConfig("http")) match {
      case Success(apiConfig) ⇒
        val tlsContext = TlsContext.load(serverConfig.getConfig("tls.keystores")).right.toOption
        start(apiConfig, tlsContext)
      case Failure(e) ⇒
        throw e
    }
  }

  def start(config: HttpApiConfig, tlsContext: Option[TlsContext])(implicit system: ActorSystem): Unit = {
    implicit val mat = ActorMaterializer(ActorMaterializerSettings(system).withAutoFusing(false))

    val status = new StatusHttpHandler
    val app = new AppFilesHttpHandler(config.staticFiles)

    def defaultRoutes: Route = app.routes ~ defaultVersion(status.routes)

    def routes = HttpApi(system).customRoutes.foldLeft(defaultRoutes)(_ ~ _)

    val defaultSettings = ServerSettings(system)

    Http().bind(
      config.interface,
      config.port,
      httpsContext = tlsContext map (_.asHttpsContext),
      settings = defaultSettings.copy(timeouts = defaultSettings.timeouts.copy(idleTimeout = IdleTimeout))
    )
      .runForeach { conn ⇒
        conn handleWith routes
      }
  }
}
