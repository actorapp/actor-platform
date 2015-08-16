package im.actor.server.activation.internal

import im.actor.server.activation.Activation.Code
import im.actor.server.activation.{ Validated, ValidationResponse }
import slick.dbio.DBIO

import scalaz.{ \/, \/- }

trait CodeActivation {
  def send(optTransactionHash: Option[String], code: Code): DBIO[String \/ Unit]
  def validate(transactionHash: String, code: String): DBIO[ValidationResponse]
  def finish(transactionHash: String): DBIO[Unit]
}
