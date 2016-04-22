package im.actor.server.activation

import akka.actor.ActorSystem
import cats.data.Xor
import im.actor.server.activation.common._
import im.actor.server.db.DbExtension
import im.actor.server.model.{ AuthEmailTransaction, AuthPhoneTransaction, AuthTransactionBase, ExpirableCode }
import im.actor.util.cache.CacheHelpers

import scala.concurrent.Future

final class ActivationContext(implicit system: ActorSystem) extends CodeGen {

  import system.dispatcher
  import ActivationProviders._
  import CacheHelpers._

  private val db = DbExtension(system).db
  private val providers = getProviders()

  require(providers exists { case (k, _) ⇒ k != Internal }, "Should be at least one external activation provider")

  private val optInternalProvider = providers.get(Internal)
  private val optSmsProvider = providers.get(Sms)
  private val optCallProvider = providers.get(Call)
  private val optSmtpProvider = providers.get(Smtp)

  private val MaxCacheSize = 1000L
  private implicit val codesCache = createCache[String, Code](MaxCacheSize)

  /**
   * We don't care about result of sending internal code.
   * But we do care about sending code via external provider.
   * We also don't show "Try to send code later" warning to end users.
   */
  def send(txHash: String, codeTemplate: Code): Future[CodeFailure Xor Unit] = {
    val code = getCachedOrElsePut(txHash, generateCode(codeTemplate))
    (for {
      _ ← trySend(optInternalProvider, txHash, code, logFailure = false)
      result ← code match {
        case s: SmsCode   ⇒ trySend(optSmsProvider, txHash, s)
        case e: EmailCode ⇒ trySend(optSmtpProvider, txHash, e)
        case c: CallCode  ⇒ trySend(optCallProvider, txHash, c)
      }
    } yield result) map {
      case Xor.Left(BadRequest(message)) ⇒
        system.log.warning("Bad request. Message: {}. Tx hash: {}, code: {}", message, txHash, code)
        Xor.Right(())
      case error @ Xor.Left(SendFailure(message)) ⇒
        system.log.error("Send failure. Message: {}. Tx hash: {}, code: {}", message, txHash, code)
        error
      case result: Xor.Right[_] ⇒ result
    }
  }

  /**
   * If internal code validates - we are fine.
   * Otherwise - validate code sent via external provider.
   */
  def validate(tx: AuthTransactionBase with ExpirableCode, code: String): Future[ValidationResponse] =
    for {
      internalResp ← tryValidate(optInternalProvider, tx.transactionHash, code, logFailure = false)
      result ← if (internalResp == Validated) {
        Future.successful(internalResp)
      } else {
        for {
          resp ← tx match {
            case _: AuthEmailTransaction ⇒ tryValidate(optSmtpProvider, tx.transactionHash, code)
            case _: AuthPhoneTransaction ⇒ tryValidate(optSmsProvider, tx.transactionHash, code)
          }
        } yield resp
      }
    } yield result

  /**
   * It is required to cleanup both internal and external provider.
   */
  def cleanup(tx: AuthTransactionBase with ExpirableCode): Future[Unit] =
    for {
      _ ← tryCleanup(optInternalProvider, tx.transactionHash)
      _ ← for {
        resp ← tx match {
          case _: AuthEmailTransaction ⇒ tryCleanup(optSmtpProvider, tx.transactionHash)
          case _: AuthPhoneTransaction ⇒ tryCleanup(optSmsProvider, tx.transactionHash)
        }
      } yield ()
      _ = codesCache.invalidate(tx.transactionHash)
    } yield ()

  private def trySend(optProvider: Option[ActivationProvider], txHash: String, code: Code, logFailure: Boolean = true): Future[CodeFailure Xor Unit] =
    optProvider map (_.send(txHash, code)) getOrElse {
      if (logFailure) { system.log.error(s"No provider found to handle code of type {}", code.getClass) }
      Future.successful(Xor.left(SendFailure(s"No provider found to handle code of type ${code.getClass}")))
    }

  private def tryValidate(optProvider: Option[ActivationProvider], txHash: String, code: String, logFailure: Boolean = true): Future[ValidationResponse] =
    optProvider map (_.validate(txHash, code)) getOrElse {
      if (logFailure) { system.log.error(s"No provider found to validate code") }
      Future.successful(InternalError)
    }

  private def tryCleanup(optProvider: Option[ActivationProvider], txHash: String): Future[Unit] =
    optProvider map (_.cleanup(txHash)) getOrElse Future.successful(())
}