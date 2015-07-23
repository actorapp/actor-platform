package im.actor.server.persist

import com.github.tototoshi.slick.PostgresJodaSupport._
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import im.actor.server.models

class AuthSessionTable(tag: Tag) extends Table[models.AuthSession](tag, "auth_sessions") {
  def userId = column[Int]("user_id", O.PrimaryKey)

  def id = column[Int]("id", O.PrimaryKey)

  def appId = column[Int]("app_id")

  def appTitle = column[String]("app_title")

  def deviceTitle = column[String]("device_title")

  def authTime = column[DateTime]("auth_time")

  def authLocation = column[String]("auth_location")

  def latitude = column[Option[Double]]("latitude")

  def longitude = column[Option[Double]]("longitude")

  def authId = column[Long]("auth_id")

  def deviceHash = column[Array[Byte]]("device_hash")

  def deletedAt = column[Option[DateTime]]("deleted_at")

  def * =
    (userId, id, authId, appId, appTitle, deviceTitle, deviceHash, authTime, authLocation, latitude, longitude) <>
      ((models.AuthSession.apply _).tupled, models.AuthSession.unapply)
}

object AuthSession {
  val sessions = TableQuery[AuthSessionTable]

  val activeSessions = sessions.filter(_.deletedAt.isEmpty)

  def create(session: models.AuthSession) =
    sessions += session

  def find(userId: Int, id: Int) =
    activeSessions.filter(s ⇒ s.userId === userId && s.id === id).result

  def findByUserId(userId: Int) =
    activeSessions.filter(_.userId === userId).result

  def findByAuthId(authId: Long) =
    activeSessions.filter(_.authId === authId).result.headOption

  def findAppIdByAuthId(authId: Long) =
    activeSessions.filter(_.authId === authId).map(_.appId).result.headOption

  def findByDeviceHash(deviceHash: Array[Byte]) =
    activeSessions.filter(_.deviceHash === deviceHash).result

  def delete(userId: Int, id: Int) =
    activeSessions.filter(s ⇒ s.userId === userId && s.id === id).map(_.deletedAt).update(Some(new DateTime))
}
