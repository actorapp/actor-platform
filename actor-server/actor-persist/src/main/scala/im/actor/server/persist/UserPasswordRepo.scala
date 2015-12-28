package im.actor.server.persist

import com.google.protobuf.ByteString
import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.UserPassword

final class UserPasswordTable(tag: Tag) extends Table[UserPassword](tag, "user_passwords") {
  def userId = column[Int]("user_id", O.PrimaryKey)

  def salt = column[ByteString]("salt")

  def hash = column[ByteString]("hash")

  def * = (userId, salt, hash) <> ((UserPassword.apply _).tupled, UserPassword.unapply)
}

object UserPasswordRepo {
  val userPasswords = TableQuery[UserPasswordTable]

  val byUserId = Compiled { (userId: Rep[Int]) ⇒
    userPasswords filter (_.userId === userId) take 1
  }

  def createOrReplace(userId: Int, salt: Array[Byte], hash: Array[Byte]) =
    userPasswords.insertOrUpdate(UserPassword(userId, ByteString.copyFrom(salt), ByteString.copyFrom(hash)))

  def find(userId: Int) = byUserId(userId).result.headOption
}
