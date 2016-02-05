package im.actor.server.activation

import akka.actor.ActorSystem
import akka.util.Timeout
import cats.data.Xor
import im.actor.server.activation.common._
import shardakka.ShardakkaExtension

import scala.concurrent.Future
import scala.concurrent.duration._

final class ActivationContext(implicit system: ActorSystem) {

  import system.dispatcher
  import ActivationProviders._

  implicit val timeout = Timeout(20.seconds)
  private val transactionsKV = ShardakkaExtension(system).simpleKeyValue("activation-transactions")

  private val providers = getProviders()

  require(providers exists { case (k, _) ⇒ k != InApp }, "Should be at least one external activation provider")

  //  private val inAppProvider = providers.get(InApp)
  private val optSmsProvider = providers.get(Sms)
  private val optCallProvider = providers.get(Call)
  private val optSmtpProvider = providers.get(Smtp)

  def send(txHash: String, code: Code): Future[CodeFailure Xor Unit] = code match {
    case s: SmsCode   ⇒ trySend(optSmsProvider, txHash, s)
    case e: EmailCode ⇒ trySend(optSmtpProvider, txHash, e)
    case c: CallCode  ⇒ trySend(optCallProvider, txHash, c)
  }

  def validate(txHash: String, code: String): Future[ValidationResponse] =
    for {
      optType ← transactionsKV.get(txHash)
      resp ← optType match {
        case None ⇒ Future.successful(InvalidHash)
        case Some(typ) ⇒ typ match {
          case Sms  ⇒ tryValidate(optSmsProvider, txHash, code, Sms)
          case Call ⇒ tryValidate(optCallProvider, txHash, code, Call)
          case Smtp ⇒ tryValidate(optSmtpProvider, txHash, code, Smtp)
        }
      }
    } yield resp

  def cleanup(txHash: String): Future[Unit] =
    for {
      optType ← transactionsKV.get(txHash)
      resp ← optType match {
        case None ⇒ Future.successful(())
        case Some(typ) ⇒ typ match {
          case Sms  ⇒ tryCleanup(optSmsProvider, txHash, Sms)
          case Call ⇒ tryCleanup(optCallProvider, txHash, Call)
          case Smtp ⇒ tryCleanup(optSmtpProvider, txHash, Smtp)
        }
      }
    } yield ()

  private def trySend(optProvider: Option[ActivationProvider], txHash: String, code: Code): Future[CodeFailure Xor Unit] =
    optProvider map { provider ⇒
      for {
        result ← provider.send(txHash, code)
        _ ← transactionsKV.upsert(txHash, codeToCodeType(code))
      } yield result
    } getOrElse {
      system.log.error(s"No provider found to handle code of type {}", code.getClass)
      Future.successful(Xor.left(SendFailure(s"No provider found to handle code of type ${code.getClass}")))
    }

  private def tryValidate(optProvider: Option[ActivationProvider], txHash: String, code: String, codeType: String): Future[ValidationResponse] =
    optProvider map (_.validate(txHash, code)) getOrElse {
      system.log.error(s"No provider found to handle code of type $codeType")
      Future.successful(InternalError)
    }

  private def tryCleanup(optProvider: Option[ActivationProvider], txHash: String, codeType: String): Future[Unit] =
    optProvider map { provider ⇒
      for {
        _ ← provider.cleanup(txHash)
        _ ← transactionsKV.delete(txHash)
      } yield ()
    } getOrElse {
      system.log.error(s"No provider found to handle code of type $codeType")
      Future.successful(())
    }

  private def codeToCodeType: PartialFunction[Code, String] = {
    case _: SmsCode   ⇒ Sms
    case _: CallCode  ⇒ Call
    case _: EmailCode ⇒ Smtp
  }

}