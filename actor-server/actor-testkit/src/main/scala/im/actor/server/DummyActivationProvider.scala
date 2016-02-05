package im.actor.server

import akka.actor.ActorSystem
import cats.data.Xor
import im.actor.server.activation.common._
import im.actor.server.db.DbExtension
import im.actor.server.persist.AuthCodeRepo

import scala.concurrent.{ ExecutionContext, Future }

final class DummyActivationProvider(system: ActorSystem) extends ActivationProvider with CommonAuthCodes {

  protected val db = DbExtension(system).db
  override protected val activationConfig: ActivationConfig = null
  override protected implicit val ec: ExecutionContext = system.dispatcher

  override def send(txHash: String, code: Code): Future[CodeFailure Xor Unit] = db.run(AuthCodeRepo.createOrUpdate(txHash, code.code)) map (_ ⇒ Xor.right(()))

}