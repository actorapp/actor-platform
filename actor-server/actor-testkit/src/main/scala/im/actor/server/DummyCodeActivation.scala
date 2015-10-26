package im.actor.server

import im.actor.server.activation.Activation.Code
import im.actor.server.activation.{ CodeFailure, CodeActivation, Validated, ValidationResponse }
import slick.dbio._

import scalaz.{ \/, \/- }

final class DummyCodeActivation extends CodeActivation {
  override def send(transactionHash: Option[String], code: Code): DBIO[CodeFailure \/ Unit] = DBIO.successful(\/-(()))
  override def validate(codeHash: String, code: String): DBIO[ValidationResponse] = DBIO.successful(Validated)
  def finish(transactionHash: String): DBIO[Unit] = DBIO.successful(())
}