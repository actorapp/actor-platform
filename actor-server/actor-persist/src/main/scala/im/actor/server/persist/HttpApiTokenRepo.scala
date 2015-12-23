package im.actor.server.persist

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.HttpApiToken

final class HttpApiTokenTable(tag: Tag) extends Table[HttpApiToken](tag, "http_api_tokens") {
  def token = column[String]("token", O.PrimaryKey)

  def isAdmin = column[Boolean]("is_admin")

  def * = (token, isAdmin) <> ((HttpApiToken.apply _).tupled, HttpApiToken.unapply)
}

object HttpApiTokenRepo {
  val httpApiTokens = TableQuery[HttpApiTokenTable]

  val byToken = Compiled { (token: Rep[String]) ⇒
    httpApiTokens filter (_.token === token)
  }

  def find(token: String) = byToken(token).result

  def fetchAll = httpApiTokens.result

  def create(token: String, isAdmin: Boolean) = httpApiTokens += HttpApiToken(token, isAdmin)
}