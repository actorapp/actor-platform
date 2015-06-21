package im.actor.server.persist

import scala.concurrent.ExecutionContext

import com.github.tototoshi.slick.PostgresJodaSupport._
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import im.actor.server.models

class AuthIdTable(tag: Tag) extends Table[models.AuthId](tag, "auth_ids") {
  def id = column[Long]("id", O.PrimaryKey)

  def userId = column[Option[Int]]("user_id")

  def publicKeyHash = column[Option[Long]]("public_key_hash")

  def deletedAt = column[Option[DateTime]]("deleted_at")

  def * = (id, userId, publicKeyHash) <> (models.AuthId.tupled, models.AuthId.unapply)
}

object AuthId {
  val authIds = TableQuery[AuthIdTable]

  val activeAuthIds = authIds.filter(_.deletedAt.isEmpty)

  def create(authId: Long, userId: Option[Int], publicKeyHash: Option[Long]) =
    authIds += models.AuthId(authId, userId, publicKeyHash)

  def byAuthIdNotDeleted(authId: Long) =
    activeAuthIds.filter(a ⇒ a.id === authId)

  def setUserData(authId: Long, userId: Int) =
    byAuthIdNotDeleted(authId).map(a ⇒ a.userId).update(Some(userId))

  def setUserData(authId: Long, userId: Int, publicKeyHash: Long) =
    byAuthIdNotDeleted(authId).map(a ⇒ (a.userId, a.publicKeyHash)).update((Some(userId), Some(publicKeyHash)))

  def find(authId: Long) =
    byAuthIdNotDeleted(authId).take(1).result.headOption

  def findUserId(authId: Long)(implicit ec: ExecutionContext) =
    byAuthIdNotDeleted(authId).map(_.userId).take(1).result.headOption map (_.flatten)

  def findByUserId(userId: Int) =
    activeAuthIds.filter(a ⇒ a.userId === userId).result

  def findIdByUserId(userId: Int) =
    activeAuthIds.filter(a ⇒ a.userId === userId).map(_.id).result

  def findIdByUserIds(userIds: Set[Int]) =
    activeAuthIds.filter(a ⇒ a.userId inSet userIds).map(_.id).result

  def delete(id: Long) =
    activeAuthIds.filter(_.id === id).map(_.deletedAt).update(Some(new DateTime))
}
