package im.actor.server.activation.magfa

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import cats.data.Xor
import im.actor.config.ActorConfig
import im.actor.server.activation.common.ActivationStateActor.{ ForgetSentCode, Send, SendAck }
import im.actor.server.activation.common._
import im.actor.server.db.DbExtension
import im.actor.server.model.AuthPhoneTransaction
import im.actor.server.persist.auth.AuthTransactionRepo
import im.actor.server.sms._
import im.actor.util.misc.PhoneNumberUtils.isTestPhone

import scala.concurrent.Future
import scala.concurrent.duration._

private[activation] final class MagfaProvider(implicit system: ActorSystem) extends ActivationProvider with CommonAuthCodes {

  protected val activationConfig = ActivationConfig.load.getOrElse(throw new RuntimeException("Failed to load activation config"))
  protected val db = DbExtension(system).db
  protected implicit val ec = system.dispatcher

  private val telesignClient = new MagfaClient(ActorConfig.load().getConfig("services.magfa"))
  private val smsEngine = new MagfaSmsEngine(telesignClient)

  private implicit val timeout = Timeout(20.seconds)

  private val smsStateActor = system.actorOf(ActivationStateActor.props[Long, SmsCode](
    repeatLimit = activationConfig.repeatLimit,
    sendAction = (code: SmsCode) ⇒ smsEngine.sendCode(code.phone, code.code),
    id = (code: SmsCode) ⇒ code.phone
  ), "telesign-sms-state")

  override def send(txHash: String, code: Code): Future[CodeFailure Xor Unit] = code match {
    case s: SmsCode ⇒
      for {
        resp ← if (isTestPhone(s.phone))
          Future.successful(Xor.right(()))
        else
          (smsStateActor ? Send(code)).mapTo[SendAck].map(_.result)
        _ ← createAuthCodeIfNeeded(resp, txHash, code.code)
      } yield resp
    case other ⇒ throw new RuntimeException(s"This provider can't handle code of type: ${other.getClass}")
  }

  override def cleanup(txHash: String): Future[Unit] = {
    for {
      ac ← db.run(AuthTransactionRepo.findChildren(txHash))
      _ = ac match {
        case Some(x: AuthPhoneTransaction) ⇒
          smsStateActor ! ForgetSentCode.phone(x.phoneNumber)
        case _ ⇒
      }
      _ ← deleteAuthCode(txHash)
    } yield ()
  }

}
