package im.actor.server.persist.auth

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.AuthPhoneTransaction

class AuthPhoneTransactionTable(tag: Tag) extends AuthTransactionRepoBase[AuthPhoneTransaction](tag, "auth_phone_transactions") with InheritingTable {
  def phoneNumber = column[Long]("phone_number")

  val inherited = AuthTransactionRepo.transactions.baseTableRow
  def * = (
    phoneNumber,
    transactionHash,
    appId,
    apiKey,
    deviceHash,
    deviceTitle,
    accessSalt,
    deviceInfo,
    isChecked,
    deletedAt
  ) <> (AuthPhoneTransaction.tupled, AuthPhoneTransaction.unapply)
}

object AuthPhoneTransactionRepo {
  val phoneTransactions = TableQuery[AuthPhoneTransactionTable]

  val active = phoneTransactions.filter(_.deletedAt.isEmpty)

  val byHash = Compiled { hash: Rep[String] ⇒
    active.filter(_.transactionHash === hash)
  }

  val byPhoneAndDeviceHash = Compiled { (phone: Rep[Long], deviceHash: Rep[Array[Byte]]) ⇒
    active.filter(t ⇒ t.phoneNumber === phone && t.deviceHash === deviceHash)
  }

  def create(transaction: AuthPhoneTransaction) =
    phoneTransactions += transaction

  def find(transactionHash: String) =
    byHash(transactionHash).result.headOption

  def findByPhoneAndDeviceHash(phone: Long, deviceHash: Array[Byte]) =
    byPhoneAndDeviceHash((phone, deviceHash)).result.headOption
}
