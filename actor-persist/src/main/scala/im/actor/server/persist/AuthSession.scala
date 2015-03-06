package im.actor.server.persist

import com.github.tototoshi.slick.PostgresJodaSupport._
import im.actor.server.models
import org.joda.time.DateTime
import scodec.bits.BitVector
import slick.driver.PostgresDriver.api._, Database.dynamicSession

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
  def publicKeyHash = column[Long]("public_key_hash")
  def deviceHash = column[Array[Byte]]("device_hash")

  def * =
    (userId, id, authId, appId, appTitle, deviceTitle, deviceHash, authTime, authLocation, latitude, longitude, publicKeyHash) <>
      ((models.AuthSession.apply _).tupled, models.AuthSession.unapply)
}

object AuthSession {
  val sessions = TableQuery[AuthSessionTable]

  def create(session: models.AuthSession) =
    sessions += session
}
