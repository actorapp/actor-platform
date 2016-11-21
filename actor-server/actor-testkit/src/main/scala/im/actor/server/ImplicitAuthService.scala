package im.actor.server

import akka.actor.ActorSystem
import akka.stream.Materializer
import im.actor.server.api.rpc.service.auth.AuthServiceImpl
import im.actor.server.oauth.{ GoogleProvider, OAuth2GoogleConfig }
import im.actor.server.session.SessionRegion
import slick.driver.PostgresDriver.api._

trait ImplicitAuthService {
  protected implicit val system: ActorSystem
  protected implicit val materializer: Materializer
  protected implicit val db: Database
  protected implicit val sessionRegion: SessionRegion

  protected val oauthConfig = OAuth2GoogleConfig(
    "http://localhost:3000/o/oauth2/auth",
    "http://localhost:3000",
    "http://localhost:3000",
    "actor",
    "AA1865139A1CACEABFA45E6635AA7761",
    "https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile"
  )
  implicit lazy val authService = {
    val oauth2Service = new GoogleProvider(oauthConfig)
    new AuthServiceImpl(oauth2Service)
  }
}
