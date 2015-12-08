package im.actor.server.activation

import im.actor.server.activation.Activation.Code
import slick.dbio.DBIO

import scalaz.\/

trait CodeActivation {
  def send(optTransactionHash: Option[String], code: Code): DBIO[CodeFailure \/ Unit]
  def validate(transactionHash: String, code: String): DBIO[ValidationResponse]
  def finish(transactionHash: String): DBIO[Unit]
}
