package im.actor.server.oauth

import im.actor.server.model.OAuth2Token
import slick.dbio._

trait OAuth2Provider {

  def completeOAuth(code: String, userId: String, redirectUri: Option[String]): DBIO[Option[OAuth2Token]]

  def getAuthUrl(redirectUrl: String, userId: String): Option[String]

  def refreshToken(userId: String): DBIO[Option[OAuth2Token]]
}
