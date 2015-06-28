package im.actor.server.oauth

import slick.dbio._

import im.actor.server.models

trait OAuth2Provider {

  def completeOAuth(code: String, userId: String, redirectUri: Option[String]): DBIO[Option[models.OAuth2Token]]

  def getAuthUrl(redirectUrl: String, userId: String): Option[String]

  def refreshToken(userId: String): DBIO[Option[models.OAuth2Token]]
}
