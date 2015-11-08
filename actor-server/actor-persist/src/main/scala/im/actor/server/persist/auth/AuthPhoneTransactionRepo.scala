package im.actor.server.persist.auth

import im.actor.server.db.ActorPostgresDriver.api._

import im.actor.server.model

class AuthPhoneTransactionTable(tag: Tag) extends AuthTransactionBase[model.AuthPhoneTransaction](tag, "auth_phone_transactions") with InheritingTable {
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
  ) <> (model.AuthPhoneTransaction.tupled, model.AuthPhoneTransaction.unapply)
}

object AuthPhoneTransactionRepo {
  val phoneTransactions = TableQuery[AuthPhoneTransactionTable]

  val active = phoneTransactions.filter(_.deletedAt.isEmpty)

  def create(transaction: model.AuthPhoneTransaction) =
    phoneTransactions += transaction

  def find(transactionHash: String) =
    active.filter(_.transactionHash === transactionHash).result.headOption

  def findByPhoneAndDeviceHash(phone: Long, deviceHash: Array[Byte]) =
    active.filter(t ⇒ t.phoneNumber === phone && t.deviceHash === deviceHash).result.headOption
}
