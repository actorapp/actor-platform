package im.actor.server.persist.auth

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.AuthUsernameTransaction

final class AuthUsernameTransactionTable(tag: Tag)
  extends AuthTransactionRepoBase[AuthUsernameTransaction](tag, "auth_username_transactions")
  with InheritingTable {
  def username = column[String]("username")
  def userId = column[Option[Int]]("user_id")

  val inherited = AuthTransactionRepo.transactions.baseTableRow

  def * = (
    username,
    userId,
    transactionHash,
    appId,
    apiKey,
    deviceHash,
    deviceTitle,
    accessSalt,
    deviceInfo,
    isChecked,
    deletedAt
  ) <> (AuthUsernameTransaction.tupled, AuthUsernameTransaction.unapply)
}

object AuthUsernameTransactionRepo {
  val usernameTransactions = TableQuery[AuthUsernameTransactionTable]

  val active = usernameTransactions filter (_.deletedAt.isEmpty)

  def byHash(hash: String) = active.filter(_.transactionHash === hash)

  val byUsernameAndDeviceHash = Compiled { (username: Rep[String], deviceHash: Rep[Array[Byte]]) ⇒
    active.filter(t ⇒ t.username === username && t.deviceHash === deviceHash)
  }

  def create(authTransaction: AuthUsernameTransaction) = usernameTransactions += authTransaction

  def find(username: String, deviceHash: Array[Byte]) = byUsernameAndDeviceHash((username, deviceHash)).result.headOption

  def find(hash: String) = byHash(hash).result.headOption

  def setUserId(hash: String, userId: Int) =
    byHash(hash).map(_.userId).update(Some(userId))
}