package im.actor.server.persist.auth

import im.actor.server.db.ActorPostgresDriver.api._

import im.actor.server.models

class AuthEmailTransactionTable(tag: Tag) extends AuthTransactionBase[models.AuthEmailTransaction](tag, "auth_email_transactions") with InheritingTable {
  def email = column[String]("email")
  def redirectUri = column[Option[String]]("redirect_uri")

  val inherited = AuthTransaction.transactions.baseTableRow
  def * = (email, redirectUri, transactionHash, appId, apiKey, deviceHash, deviceTitle, accessSalt, isChecked, deletedAt) <> (models.AuthEmailTransaction.tupled, models.AuthEmailTransaction.unapply)
}

object AuthEmailTransaction {
  val emailTransactions = TableQuery[AuthEmailTransactionTable]

  val active = emailTransactions.filter(_.deletedAt.isEmpty)

  def create(transaction: models.AuthEmailTransaction) =
    emailTransactions += transaction

  def find(transactionHash: String) =
    active.filter(_.transactionHash === transactionHash).result.headOption

  def findByEmail(email: String) =
    active.filter(_.email === email).result.headOption

  def updateRedirectUri(transactionHash: String, redirectUri: String) =
    emailTransactions.filter(_.transactionHash === transactionHash).map(_.redirectUri).update(Some(redirectUri))
}
