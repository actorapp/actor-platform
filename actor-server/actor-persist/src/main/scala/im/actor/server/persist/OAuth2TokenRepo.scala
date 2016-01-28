package im.actor.server.persist

import java.time.LocalDateTime

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.OAuth2Token

final class OAuth2TokenTable(tag: Tag) extends Table[OAuth2Token](tag, "oauth2_tokens") {
  def id = column[Long]("id", O.PrimaryKey)
  def userId = column[String]("user_id", O.PrimaryKey)
  def accessToken = column[String]("access_token")
  def tokenType = column[String]("token_type")
  def expiresIn = column[Long]("expires_in")
  def refreshToken = column[Option[String]]("refresh_token")
  def createdAt = column[LocalDateTime]("created_at")

  def * = (id, userId, accessToken, tokenType, expiresIn, refreshToken, createdAt) <> (OAuth2Token.tupled, OAuth2Token.unapply)
}

object OAuth2TokenRepo {
  val tokens = TableQuery[OAuth2TokenTable]

  def create(token: OAuth2Token) =
    tokens += token

  def createOrUpdate(token: OAuth2Token) =
    tokens.insertOrUpdate(token)

  def findByUserId(userId: String) =
    tokens.filter(_.userId === userId).sortBy(_.createdAt.desc).result.headOption

  def findRefreshToken(userId: String) =
    tokens.filter(t ⇒ t.userId === userId && t.refreshToken.isDefined).result.headOption

}
