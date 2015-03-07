package im.actor.server.persist

import com.github.tototoshi.slick.PostgresJodaSupport._
import im.actor.server.models
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._
import shapeless._, syntax.sized._

class UserPublicKeyTable(tag: Tag) extends Table[models.UserPublicKey](tag, "public_keys") {
  def userId = column[Int]("user_id", O.PrimaryKey)
  def hash = column[Long]("hash", O.PrimaryKey)
  def data = column[Array[Byte]]("data")
  def authId = column[Long]("auth_id")
  def deletedAt = column[Option[DateTime]]("deleted_at")

  def * = (userId, hash, data, authId) <> (models.UserPublicKey.tupled, models.UserPublicKey.unapply)
}

object UserPublicKey {
  val pkeys = TableQuery[UserPublicKeyTable]

  private def active =
    pkeys.filter(_.deletedAt.isEmpty)

  private def activeByUserId(userId: Int) =
    active.filter(p => p.userId === userId && p.deletedAt.isEmpty)

  def create(pk: models.UserPublicKey) =
    pkeys += pk

  def delete(userId: Int, hash: Long) =
    pkeys.filter(p => p.userId === userId && p.hash === hash).map(_.deletedAt).update(Some(new DateTime))

  def findKeyHashes(userId: Int)  =
    activeByUserId(userId).map(_.hash).result

  def find(userId: Int, authId: Long) =
    active.filter(p => p.userId === userId && p.authId === authId).result

  def findByUserHashes(pairs: Set[(Int, Long)]) = {
    // TODO: type-based size checking
    require(pairs.size > 0)

    active.filter { pk =>
      pairs.view.map {
        case (userId, hash) => pk.userId === userId && pk.hash === hash
      }.reduceLeft(_ || _)
    }.result
  }
}
