package im.actor.server.persist

import com.github.tototoshi.slick.PostgresJodaSupport._
import im.actor.server.model.AuthSession
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

final class AuthSessionTable(tag: Tag) extends Table[AuthSession](tag, "auth_sessions") {
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
      ((AuthSession.apply _).tupled, AuthSession.unapply)
}

object AuthSessionRepo {
  val sessions = TableQuery[AuthSessionTable]

  val activeSessions = sessions.filter(_.deletedAt.isEmpty)

  def byDeviceHash(deviceHash: Rep[Array[Byte]]) =
    activeSessions.filter(_.deviceHash === deviceHash)
  val byDeviceHashC = Compiled(byDeviceHash _)

  val byAuthId = Compiled { (authId: Rep[Long]) ⇒
    activeSessions.filter(_.authId === authId)
  }

  val appIdByAuthId = Compiled { (authId: Rep[Long]) ⇒
    activeSessions.filter(_.authId === authId).map(_.appId)
  }

  val byUserIdAndId = Compiled { (userId: Rep[Int], id: Rep[Int]) ⇒
    activeSessions.filter(s ⇒ s.userId === userId && s.id === id)
  }

  val byUserId = Compiled { userId: Rep[Int] ⇒
    activeSessions.filter(_.userId === userId)
  }

  def idsByAuthIds(authIds: Set[Long]) =
    activeSessions.filter(_.authId.inSet(authIds)).map(as ⇒ as.authId → as.id)

  def create(session: AuthSession) =
    sessions += session

  def find(userId: Int, id: Int) =
    byUserIdAndId((userId, id)).result

  def findByUserId(userId: Int) =
    byUserId(userId).result

  def findFirstByUserId(userId: Int) =
    activeSessions.filter(_.userId === userId).take(1).result.headOption

  def findByAuthId(authId: Long) =
    byAuthId(authId).result.headOption

  def findAppIdByAuthId(authId: Long) =
    appIdByAuthId(authId).result.headOption

  def findByDeviceHash(deviceHash: Array[Byte]) =
    byDeviceHashC(deviceHash).result

  def findIdsByAuthIds(authIds: Set[Long]) = idsByAuthIds(authIds).result

  def delete(userId: Int, id: Int) =
    activeSessions.filter(s ⇒ s.userId === userId && s.id === id).map(_.deletedAt).update(Some(new DateTime))
}
