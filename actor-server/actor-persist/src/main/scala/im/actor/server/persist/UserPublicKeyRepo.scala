package im.actor.server.persist

import com.github.tototoshi.slick.PostgresJodaSupport._
import im.actor.server.model.UserPublicKey
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

final class UserPublicKeyTable(tag: Tag) extends Table[UserPublicKey](tag, "public_keys") {
  def userId = column[Int]("user_id", O.PrimaryKey)
  def hash = column[Long]("hash", O.PrimaryKey)
  def data = column[Array[Byte]]("data")
  def deletedAt = column[Option[DateTime]]("deleted_at")

  def * = (userId, hash, data) <> (UserPublicKey.tupled, UserPublicKey.unapply)
}

object UserPublicKeyRepo {
  val pkeys = TableQuery[UserPublicKeyTable]

  private def active =
    pkeys.filter(_.deletedAt.isEmpty)

  private def activeByUserId(userId: Int) =
    active.filter(p ⇒ p.userId === userId && p.deletedAt.isEmpty)

  def create(pk: UserPublicKey) =
    pkeys += pk

  def delete(userId: Int, hash: Long) =
    pkeys.filter(p ⇒ p.userId === userId && p.hash === hash).map(_.deletedAt).update(Some(new DateTime))

  def find(userId: Int, hash: Long) =
    active.filter(p ⇒ p.userId === userId && p.hash === hash).result

  def findByUserId(userId: Int) =
    active.filter(_.userId === userId).result

  def findKeyHashes(userId: Int) =
    activeByUserId(userId).map(_.hash).result

  def findByUserHashes(pairs: Set[(Int, Long)]) = {
    // TODO: type-based size checking
    require(pairs.size > 0)

    active.filter { pk ⇒
      pairs.view.map {
        case (userId, hash) ⇒ pk.userId === userId && pk.hash === hash
      }.reduceLeft(_ || _)
    }.result
  }
}
