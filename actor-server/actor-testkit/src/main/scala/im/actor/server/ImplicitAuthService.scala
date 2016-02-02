package im.actor.server

import akka.actor.ActorSystem
import akka.stream.Materializer
import im.actor.server.activation.ActivationContext
import im.actor.server.api.rpc.service.auth.AuthServiceImpl
import im.actor.server.oauth.{ GoogleProvider, OAuth2GoogleConfig }
import im.actor.server.session.SessionRegion
import slick.driver.PostgresDriver.api._

trait ImplicitAuthService {
  protected implicit val system: ActorSystem
  protected implicit val materializer: Materializer
  protected implicit val db: Database
  protected implicit val sessionRegion: SessionRegion

  private val oauthGoogleConfig = OAuth2GoogleConfig.load(system.settings.config.getConfig("services.google.oauth"))
  private implicit lazy val oauth2Service = new GoogleProvider(oauthGoogleConfig)

  implicit lazy val authService = new AuthServiceImpl
}
