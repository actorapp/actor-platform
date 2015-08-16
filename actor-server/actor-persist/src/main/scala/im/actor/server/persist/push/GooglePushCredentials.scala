package im.actor.server.persist.push

import scala.concurrent.ExecutionContext

import slick.driver.PostgresDriver.api._

import im.actor.server.models

class GooglePushCredentialsTable(tag: Tag) extends Table[models.push.GooglePushCredentials](tag, "google_push_credentials") {
  def authId = column[Long]("auth_id", O.PrimaryKey)

  def projectId = column[Long]("project_id")

  def regId = column[String]("reg_id")

  def * = (authId, projectId, regId) <> (models.push.GooglePushCredentials.tupled, models.push.GooglePushCredentials.unapply)
}

object GooglePushCredentials {
  val creds = TableQuery[GooglePushCredentialsTable]

  def createOrUpdate(authId: Long, projectId: Long, regId: String)(implicit ec: ExecutionContext) = {
    for {
      _ ← creds.filterNot(_.authId === authId).filter(c ⇒ c.projectId === projectId && c.regId === regId).delete
      r ← creds.insertOrUpdate(models.push.GooglePushCredentials(authId, projectId, regId))
    } yield r
  }

  def createOrUpdate(c: models.push.GooglePushCredentials) =
    creds.insertOrUpdate(c)

  def byAuthId(authId: Rep[Long]) = creds.filter(_.authId === authId)
  val byAuthIdC = Compiled(byAuthId _)

  def find(authId: Long) =
    byAuthIdC(authId).result.headOption

  def delete(authId: Long) =
    creds.filter(_.authId === authId).delete

  def deleteByToken(token: String) =
    creds.filter(_.regId === token).delete
}