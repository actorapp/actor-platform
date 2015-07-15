package im.actor.server.activation.internal

import scala.concurrent.Future
import scalaz.{ \/, \/- }

import slick.dbio.DBIO

import im.actor.server.activation.Activation.Code
import im.actor.server.activation.{ Validated, ValidationResponse }

trait CodeActivation {
  def send(optTransactionHash: Option[String], code: Code): DBIO[String \/ Unit]
  def validate(transactionHash: String, code: String): Future[ValidationResponse]
  def finish(transactionHash: String): DBIO[Unit]
}

class DummyCodeActivation extends CodeActivation {
  override def send(transactionHash: Option[String], code: Code): DBIO[String \/ Unit] = DBIO.successful(\/-(()))
  override def validate(codeHash: String, code: String): Future[ValidationResponse] = Future.successful(Validated)
  def finish(transactionHash: String): DBIO[Unit] = DBIO.successful(())
}
