package im.actor.server.persist.auth

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.models

class GateAuthCodeTable(tag: Tag) extends Table[models.auth.GateAuthCode](tag, "gate_auth_codes") {
  def transactionHash = column[String]("transaction_hash", O.PrimaryKey)
  def codeHash = column[String]("code_hash")
  def isDeleted = column[Boolean]("is_deleted")

  def * = (transactionHash, codeHash, isDeleted) <> (models.auth.GateAuthCode.tupled, models.auth.GateAuthCode.unapply)
}

object GateAuthCode {
  def codes = TableQuery[GateAuthCodeTable]

  val active = codes.filter(_.isDeleted === false)

  def create(transactionHash: String, codeHash: String) =
    codes += models.auth.GateAuthCode(transactionHash, codeHash)

  def find(transactionHash: String) =
    active.filter(_.transactionHash === transactionHash).result.headOption

  def delete(transactionHash: String) =
    codes.filter(_.transactionHash === transactionHash).map(_.isDeleted).update(true)

}
