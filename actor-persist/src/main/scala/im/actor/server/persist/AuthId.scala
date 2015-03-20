package im.actor.server.persist

import im.actor.server.models
import slick.driver.PostgresDriver.api._
import Database.dynamicSession
import org.joda.time.DateTime
import com.github.tototoshi.slick.PostgresJodaSupport._

class AuthIdTable(tag: Tag) extends Table[models.AuthId](tag, "auth_ids") {
  def id = column[Long]("id", O.PrimaryKey)
  def userId = column[Option[Int]]("user_id")
  def deletedAt = column[Option[DateTime]]("deleted_at")

  def * = (id, userId) <> (models.AuthId.tupled, models.AuthId.unapply)
}

object AuthId {
  val authIds = TableQuery[AuthIdTable]

  def create(authId: Long, userId: Option[Int]) =
    authIds += models.AuthId(authId, userId)

  def byAuthIdNotDeleted(authId: Long) =
    authIds.filter(a => a.id === authId && a.deletedAt.isEmpty)

  def setUserId(authId: Long, userId: Int) =
    byAuthIdNotDeleted(authId).map(_.userId).update(Some(userId))

  def find(authId: Long) =
    byAuthIdNotDeleted(authId).result

  def findByUserId(userId: Int) =
    authIds.filter(a => a.userId === userId && a.deletedAt.isEmpty).result

  def findIdByUserId(userId: Int) =
    authIds.filter(a => a.userId === userId && a.deletedAt.isEmpty).map(_.id).result
}
