package im.actor.server.persist.push

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

  def createOrUpdate(authId: Long, projectId: Long, regId: String) =
    creds.insertOrUpdate(models.push.GooglePushCredentials(authId, projectId, regId))

  def createOrUpdate(c: models.push.GooglePushCredentials) =
    creds.insertOrUpdate(c)

  def find(authId: Long) =
    creds.filter(_.authId === authId).result.headOption

  def delete(authId: Long) =
    creds.filter(_.authId === authId).delete
}