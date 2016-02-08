package im.actor.server.activation

import akka.actor.ActorSystem
import cats.data.Xor
import im.actor.server.activation.common._
import im.actor.server.db.DbExtension
import im.actor.server.model.{ ExpirableCode, AuthPhoneTransaction, AuthEmailTransaction, AuthTransactionBase }

import scala.concurrent.Future

final class ActivationContext(implicit system: ActorSystem) {

  import system.dispatcher
  import ActivationProviders._

  private val db = DbExtension(system).db
  private val providers = getProviders()

  require(providers exists { case (k, _) ⇒ k != InApp }, "Should be at least one external activation provider")

  private val optSmsProvider = providers.get(Sms)
  private val optCallProvider = providers.get(Call)
  private val optSmtpProvider = providers.get(Smtp)

  def send(txHash: String, code: Code): Future[CodeFailure Xor Unit] = code match {
    case s: SmsCode   ⇒ trySend(optSmsProvider, txHash, s)
    case e: EmailCode ⇒ trySend(optSmtpProvider, txHash, e)
    case c: CallCode  ⇒ trySend(optCallProvider, txHash, c)
  }

  def validate(tx: AuthTransactionBase with ExpirableCode, code: String): Future[ValidationResponse] =
    for {
      resp ← tx match {
        case _: AuthEmailTransaction ⇒ tryValidate(optSmtpProvider, tx.transactionHash, code)
        case _: AuthPhoneTransaction ⇒ tryValidate(optSmsProvider, tx.transactionHash, code)
      }
    } yield resp

  def cleanup(tx: AuthTransactionBase with ExpirableCode): Future[Unit] =
    for {
      resp ← tx match {
        case _: AuthEmailTransaction ⇒ tryCleanup(optSmtpProvider, tx.transactionHash)
        case _: AuthPhoneTransaction ⇒ tryCleanup(optSmsProvider, tx.transactionHash)
      }
    } yield ()

  private def trySend(optProvider: Option[ActivationProvider], txHash: String, code: Code): Future[CodeFailure Xor Unit] =
    optProvider map { provider ⇒
      for (result ← provider.send(txHash, code)) yield result
    } getOrElse {
      system.log.error(s"No provider found to handle code of type {}", code.getClass)
      Future.successful(Xor.left(SendFailure(s"No provider found to handle code of type ${code.getClass}")))
    }

  private def tryValidate(optProvider: Option[ActivationProvider], txHash: String, code: String): Future[ValidationResponse] =
    optProvider map (_.validate(txHash, code)) getOrElse {
      system.log.error(s"No provider found to handle code")
      Future.successful(InternalError)
    }

  private def tryCleanup(optProvider: Option[ActivationProvider], txHash: String): Future[Unit] =
    optProvider map { provider ⇒
      for (_ ← provider.cleanup(txHash)) yield ()
    } getOrElse {
      system.log.error(s"No provider found to handle code")
      Future.successful(())
    }
}