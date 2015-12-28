package im.actor.server.persist.auth

import java.time.{ ZoneOffset, LocalDateTime }

import scala.concurrent.ExecutionContext

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.{ AuthTransaction, AuthTransactionBase }

private[auth] abstract class AuthTransactionRepoBase[T](tag: Tag, tname: String) extends Table[T](tag, tname) {
  def transactionHash = column[String]("transaction_hash", O.PrimaryKey)
  def appId = column[Int]("app_id")
  def apiKey = column[String]("api_key")
  def deviceHash = column[Array[Byte]]("device_hash")
  def deviceTitle = column[String]("device_title")
  def accessSalt = column[String]("access_salt")
  def deviceInfo = column[Array[Byte]]("device_info")
  def isChecked = column[Boolean]("is_checked")
  def deletedAt = column[Option[LocalDateTime]]("deleted_at")
}

final class AuthTransactionTable(tag: Tag) extends AuthTransactionRepoBase[AuthTransaction](tag, "auth_transactions") {
  def * = (
    transactionHash,
    appId,
    apiKey,
    deviceHash,
    deviceTitle,
    accessSalt,
    deviceInfo,
    isChecked,
    deletedAt
  ) <> (AuthTransaction.tupled, AuthTransaction.unapply)
}

object AuthTransactionRepo {
  val transactions = TableQuery[AuthTransactionTable]

  val active = transactions.filter(_.deletedAt.isEmpty)

  def find(transactionHash: String) =
    active.filter(_.transactionHash === transactionHash).result.headOption

  def findChildren(transactionHash: String)(implicit ec: ExecutionContext): DBIO[Option[AuthTransactionBase]] =
    for {
      email ← AuthEmailTransactionRepo.find(transactionHash)
      phone ← AuthPhoneTransactionRepo.find(transactionHash)
    } yield (email, phone) match {
      case (Some(e), None) ⇒ email
      case (None, Some(p)) ⇒ phone
      case _               ⇒ None
    }

  def delete(transactionHash: String) =
    transactions.filter(_.transactionHash === transactionHash).map(_.deletedAt).update(Some(LocalDateTime.now(ZoneOffset.UTC)))

  def updateSetChecked(transactionHash: String) =
    transactions.filter(_.transactionHash === transactionHash).map(_.isChecked).update(true)

}
