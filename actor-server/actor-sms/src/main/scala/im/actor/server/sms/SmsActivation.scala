package im.actor.server.sms

import java.util.concurrent.TimeUnit

import scala.collection.mutable
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

import akka.actor._
import akka.http.scaladsl.Http
import akka.stream.Materializer
import com.typesafe.config._

object SmsActivation {
  private[sms] sealed trait Message
  private[sms] case class Send(authId: Long, phoneNumber: Long, code: String) extends Message
  private[sms] case class ForgetSentCode(phoneNumber: Long, code: String) extends Message

  def newContext(config: Config)(implicit system: ActorSystem, materializer: Materializer): SmsActivationContext = {
    val smsWaitIntervalMs = config.getDuration("activation.sms-wait-interval", TimeUnit.MILLISECONDS)

    SmsActivationContext(
      system.actorOf(
        Props(classOf[SmsActivation], smsWaitIntervalMs, config, materializer),
        "smsActivation"
      )
    )
  }
}

case class SmsActivationContext(smsActivationActor: ActorRef) extends ActivationContext {
  import SmsActivation._

  override def send(authId: Long, phoneNumber: Long, code: String): Unit = {
    smsActivationActor ! Send(authId, phoneNumber, code)
  }
}

class SmsActivation(smsWaitIntervalMs: Long, config: Config, implicit val materializer: Materializer) extends Actor with ActorLogging {
  import SmsActivation._

  implicit val system = context.system
  implicit val ec = context.dispatcher
  implicit val http = Http()

  private val sentCodes = new mutable.HashSet[(Long, String)]()

  private val engines = List(
    new TelesignSmsEngine(config.getConfig("telesign"))
  )

  private def codeWasNotSent(phoneNumber: Long, code: String) = !sentCodes.contains((phoneNumber, code))

  private def rememberSentCode(phoneNumber: Long, code: String) = sentCodes += ((phoneNumber, code))

  private def forgetSentCode(phoneNumber: Long, code: String) = sentCodes -= ((phoneNumber, code))

  private def forgetSentCodeAfterDelay(phoneNumber: Long, code: String) =
    context.system.scheduler.scheduleOnce(smsWaitIntervalMs.milliseconds, self, ForgetSentCode(phoneNumber, code))

  private def sendCode(authId: Long, phoneNumber: Long, code: String): Unit = {
    if (codeWasNotSent(phoneNumber, code)) {
      log.debug(s"Sending $code to $phoneNumber")

      rememberSentCode(phoneNumber, code)

      engines foreach { engine ⇒
        engine.sendCode(phoneNumber, code) andThen {
          case Success(res) ⇒
          //EventService.log(authId, phoneNumber, E.SmsSentSuccessfully(code, res))
          case Failure(e)   ⇒
          //EventService.log(authId, phoneNumber, E.SmsFailure(code, e.getMessage))
        }
      }

      forgetSentCodeAfterDelay(phoneNumber, code)
    } else {
      log.debug(s"Ignoring send $code to $phoneNumber")
    }
  }

  override def receive: Receive = {
    case Send(authId, phoneNumber, code)   ⇒ sendCode(authId, phoneNumber, code)
    case ForgetSentCode(phoneNumber, code) ⇒ forgetSentCode(phoneNumber, code)
  }
}
