package im.actor.server.oauth

import slick.dbio._

import im.actor.server.models

trait OAuth2Provider {

  def retreiveToken(code: String, clientName: String, redirectUri: Option[String]): DBIO[Option[models.OAuth2Token]]

  def getAuthUrl(redirectUrl: String, userId: String): Option[String]

  def refreshToken(clientName: String): DBIO[Option[models.OAuth2Token]]
}
