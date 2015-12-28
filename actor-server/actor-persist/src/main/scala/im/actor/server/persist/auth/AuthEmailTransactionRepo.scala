package im.actor.server.persist.auth

import im.actor.server.db.ActorPostgresDriver.api._

import im.actor.server.model

final class AuthEmailTransactionTable(tag: Tag)
  extends AuthTransactionRepoBase[model.AuthEmailTransaction](tag, "auth_email_transactions")
  with InheritingTable {
  def email = column[String]("email")
  def redirectUri = column[Option[String]]("redirect_uri")

  val inherited = AuthTransactionRepo.transactions.baseTableRow
  def * = (
    email,
    redirectUri,
    transactionHash,
    appId,
    apiKey,
    deviceHash,
    deviceTitle,
    accessSalt,
    deviceInfo,
    isChecked,
    deletedAt
  ) <> (model.AuthEmailTransaction.tupled, model.AuthEmailTransaction.unapply)
}

object AuthEmailTransactionRepo {
  val emailTransactions = TableQuery[AuthEmailTransactionTable]

  val active = emailTransactions.filter(_.deletedAt.isEmpty)

  val byHash = Compiled { hash: Rep[String] ⇒
    active.filter(_.transactionHash === hash)
  }

  val byEmailAndDeviceHash = Compiled { (email: Rep[String], deviceHash: Rep[Array[Byte]]) ⇒
    active.filter(t ⇒ t.email === email && t.deviceHash === deviceHash)
  }

  def create(transaction: model.AuthEmailTransaction) =
    emailTransactions += transaction

  def find(transactionHash: String) =
    byHash(transactionHash).result.headOption

  def findByEmailAndDeviceHash(email: String, deviceHash: Array[Byte]) =
    byEmailAndDeviceHash((email, deviceHash)).result.headOption

  def updateRedirectUri(transactionHash: String, redirectUri: String) =
    emailTransactions.filter(_.transactionHash === transactionHash).map(_.redirectUri).update(Some(redirectUri))
}
