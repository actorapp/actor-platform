package im.actor.server.persist

import im.actor.server.db.Db
import im.actor.server.models
import slick.driver.PostgresDriver.simple._
import Database.dynamicSession

class ApplePushCredentialsTable(tag: Tag) extends Table[models.ApplePushCredentials](tag, "apple_push_credentials") {
  def authId = column[Long]("auth_id", O.PrimaryKey)
  def apnsKey = column[Int]("apns_key")
  def token = column[String]("token")

  def * = (authId, apnsKey, token) <> (models.ApplePushCredentials.tupled, models.ApplePushCredentials.unapply)
}

object ApplePushCredentials {
  val table = TableQuery[ApplePushCredentialsTable]
}
