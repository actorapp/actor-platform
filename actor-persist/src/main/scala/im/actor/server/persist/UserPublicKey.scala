package im.actor.server.persist

import com.github.tototoshi.slick.PostgresJodaSupport._
import im.actor.server.models
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

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

  def create(userId: Int, hash: Long, data: Array[Byte], authId: Long) =
    pkeys += models.UserPublicKey(userId, hash, data, authId)

  def findKeyHashes(userId: Int)  =
    pkeys.filter(_.userId === userId).map(_.hash).result
}
