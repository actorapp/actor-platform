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

class DummyCodeActivation extends CodeActivation {
  override def send(transactionHash: Option[String], code: Code): DBIO[String \/ Unit] = DBIO.successful(\/-(()))
  override def validate(codeHash: String, code: String): DBIO[ValidationResponse] = DBIO.successful(Validated)
  def finish(transactionHash: String): DBIO[Unit] = DBIO.successful(())
}
