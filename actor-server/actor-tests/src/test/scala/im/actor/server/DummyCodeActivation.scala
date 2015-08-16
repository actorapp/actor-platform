package im.actor.server

import im.actor.server.activation.Activation.Code
import im.actor.server.activation.internal.CodeActivation
import im.actor.server.activation.{ Validated, ValidationResponse }
import slick.dbio._

import scalaz.{ \/, \/- }

class DummyCodeActivation extends CodeActivation {
  override def send(transactionHash: Option[String], code: Code): DBIO[String \/ Unit] = DBIO.successful(\/-(()))
  override def validate(codeHash: String, code: String): DBIO[ValidationResponse] = DBIO.successful(Validated)
  def finish(transactionHash: String): DBIO[Unit] = DBIO.successful(())
}