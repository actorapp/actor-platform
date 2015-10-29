package im.actor.server.persist.auth

import im.actor.server.db.ActorPostgresDriver.api._

import im.actor.server.models

class AuthPhoneTransactionTable(tag: Tag) extends AuthTransactionBase[models.AuthPhoneTransaction](tag, "auth_phone_transactions") with InheritingTable {
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
    userData,
    isChecked,
    deletedAt
  ) <> (models.AuthPhoneTransaction.tupled, models.AuthPhoneTransaction.unapply)
}

object AuthPhoneTransactionRepo {
  val phoneTransactions = TableQuery[AuthPhoneTransactionTable]

  val active = phoneTransactions.filter(_.deletedAt.isEmpty)

  def create(transaction: models.AuthPhoneTransaction) =
    phoneTransactions += transaction

  def find(transactionHash: String) =
    active.filter(_.transactionHash === transactionHash).result.headOption

  def findByPhoneAndDeviceHash(phone: Long, deviceHash: Array[Byte]) =
    active.filter(t ⇒ t.phoneNumber === phone && t.deviceHash === deviceHash).result.headOption
}
