package im.actor.server.persist

import java.time.LocalDateTime

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.AuthCode

final class AuthCodeTable(tag: Tag) extends Table[AuthCode](tag, "auth_codes") {
  def transactionHash = column[String]("transaction_hash", O.PrimaryKey)
  def code = column[String]("code")
  def attempts = column[Int]("attempts")
  def createdAt = column[LocalDateTime]("created_at")
  def isDeleted = column[Boolean]("is_deleted")

  def * = (transactionHash, code, attempts, createdAt, isDeleted) <> (AuthCode.tupled, AuthCode.unapply)
}

object AuthCodeRepo {
  val codes = TableQuery[AuthCodeTable]

  def create(transactionHash: String, code: String) =
    codes += AuthCode(transactionHash, code)

  def createOrUpdate(transactionHash: String, code: String) =
    codes.insertOrUpdate(AuthCode(transactionHash, code))

  def findByTransactionHash(transactionHash: String) =
    codes.filter(c ⇒ c.transactionHash === transactionHash && c.isDeleted === false).result.headOption

  def deleteByTransactionHash(transactionHash: String) =
    codes.filter(_.transactionHash === transactionHash).map(_.isDeleted).update(true)

  def incrementAttempts(transactionHash: String, currentValue: Int) =
    codes.filter(_.transactionHash === transactionHash).map(_.attempts).update(currentValue + 1)

}