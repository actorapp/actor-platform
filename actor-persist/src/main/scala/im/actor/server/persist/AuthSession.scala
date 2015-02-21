package im.actor.server.persist

import im.actor.server.db.Db
import im.actor.server.models
import slick.driver.PostgresDriver.simple._
import Database.dynamicSession
import org.joda.time.DateTime
import com.github.tototoshi.slick.PostgresJodaSupport._
import scodec.bits.BitVector

class AuthSessionTable(tag: Tag) extends Table[models.AuthSession](tag, "auth_sessions") {
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
  def deviceHash = column[BitVector]("device_hash")

  def * =
    (id, appId, appTitle, deviceTitle, authTime, authLocation, latitude, longitude, authId, publicKeyHash, deviceHash) <>
      (models.AuthSession.tupled, models.AuthSession.unapply)
}

object AuthSession {
  val table = TableQuery[AuthSessionTable]
}
