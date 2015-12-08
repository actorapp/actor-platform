package im.actor.server

import akka.dispatch.ExecutionContexts
import im.actor.server.activation.Activation.Code
import im.actor.server.activation.internal.InternalCodeActivation
import im.actor.server.activation.{ CodeFailure, CodeActivation, ValidationResponse }
import im.actor.server.persist.AuthCodeRepo
import slick.dbio._

import scalaz.{ \/, \/- }

final class DummyCodeActivation extends CodeActivation {
  private implicit val ec = ExecutionContexts.global()

  override def send(transactionHash: Option[String], code: Code): DBIO[CodeFailure \/ Unit] =
    transactionHash match {
      case Some(txHash) ⇒ AuthCodeRepo.createOrUpdate(txHash, code.code) map (_ ⇒ \/-(()))
      case None         ⇒ DBIO.successful(\/-(()))
    }

  override def validate(codeHash: String, code: String): DBIO[ValidationResponse] =
    InternalCodeActivation.validateAction(codeHash, code, 1, Long.MaxValue)

  def finish(txHash: String): DBIO[Unit] =
    InternalCodeActivation.finishAction(txHash)
}