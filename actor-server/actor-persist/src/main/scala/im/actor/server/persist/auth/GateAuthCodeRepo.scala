package im.actor.server.persist.auth

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.auth.GateAuthCode

final class GateAuthCodeTable(tag: Tag) extends Table[GateAuthCode](tag, "gate_auth_codes") {
  def transactionHash = column[String]("transaction_hash", O.PrimaryKey)
  def codeHash = column[String]("code_hash")
  def isDeleted = column[Boolean]("is_deleted")

  def * = (transactionHash, codeHash, isDeleted) <> (GateAuthCode.tupled, GateAuthCode.unapply)
}

object GateAuthCodeRepo {
  def codes = TableQuery[GateAuthCodeTable]

  val active = codes.filter(_.isDeleted === false)

  def createOrUpdate(transactionHash: String, codeHash: String) =
    codes.insertOrUpdate(GateAuthCode(transactionHash, codeHash))

  def find(transactionHash: String) =
    active.filter(_.transactionHash === transactionHash).result.headOption

  def delete(transactionHash: String) =
    codes.filter(_.transactionHash === transactionHash).map(_.isDeleted).update(true)

}
