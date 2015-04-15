package im.actor.server.persist

import com.github.tototoshi.slick.PostgresJodaSupport._
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import im.actor.server.models

class AuthIdTable(tag: Tag) extends Table[models.AuthId](tag, "auth_ids") {
  def id = column[Long]("id", O.PrimaryKey)

  def userId = column[Option[Int]]("user_id")

  def publicKeyHash = column[Option[Long]]("public_key_hash")

  def deletedAt = column[Option[DateTime]]("deleted_at")

  def * = (id, userId, publicKeyHash) <>(models.AuthId.tupled, models.AuthId.unapply)
}

object AuthId {
  val authIds = TableQuery[AuthIdTable]

  def create(authId: Long, userId: Option[Int], publicKeyHash: Option[Long]) =
    authIds += models.AuthId(authId, userId, publicKeyHash)

  def byAuthIdNotDeleted(authId: Long) =
    authIds.filter(a => a.id === authId && a.deletedAt.isEmpty)

  def setUserData(authId: Long, userId: Int, publicKeyHash: Long) =
    byAuthIdNotDeleted(authId).map(a => (a.userId, a.publicKeyHash)).update((Some(userId), Some(publicKeyHash)))

  def find(authId: Long) =
    byAuthIdNotDeleted(authId).result

  def findByUserId(userId: Int) =
    authIds.filter(a => a.userId === userId && a.deletedAt.isEmpty).result

  def findIdByUserId(userId: Int) =
    authIds.filter(a => a.userId === userId && a.deletedAt.isEmpty).map(_.id).result

  def findIdByUserIds(userIds: Set[Int]) =
    authIds.filter(a => a.userId inSet userIds).map(_.id).result
}
