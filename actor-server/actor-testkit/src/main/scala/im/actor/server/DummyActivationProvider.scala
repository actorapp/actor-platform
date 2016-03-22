package im.actor.server

import akka.actor.ActorSystem
import cats.data.Xor
import im.actor.server.activation.common._
import im.actor.server.db.DbExtension
import im.actor.server.persist.AuthCodeRepo

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._

final class DummyActivationProvider(system: ActorSystem) extends ActivationProvider with CommonAuthCodes {

  protected val db = DbExtension(system).db
  override protected val activationConfig: ActivationConfig = ActivationConfig(1.minute, 24.hours, 3)
  override protected implicit val ec: ExecutionContext = system.dispatcher

  override def send(txHash: String, code: Code): Future[CodeFailure Xor Unit] = db.run(AuthCodeRepo.createOrUpdate(txHash, code.code)) map (_ ⇒ Xor.right(()))

  override def cleanup(txHash: String): Future[Unit] = deleteAuthCode(txHash)
}