package im.actor.server.sms

import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.{ Base64, Date }
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

import akka.actor._
import com.typesafe.config._
import dispatch._

class TelesignSmsEngine(config: Config)(implicit system: ActorSystem) extends AuthSmsEngine {
  system.log.error("=========================================TelesignSmsEngine")
  private val apiKey = config.getString("api-key")
  private val customerId = config.getString("customer-id")
  system.log.error("=======================TelesignSmsEngine sendCode apiKey:{} customerId:{}", apiKey, customerId)
  private val resourceUrl = url("http://utf8.sms.webchinese.cn/")

  private val Utf8Encoding = "UTF-8"
  val FormContentType = "application/x-www-form-urlencoded;charset=utf-8"

  private val http = new Http()

  implicit val ec: ExecutionContext = system.dispatcher

  override def sendCode(phoneNumber: Long, message: String): Future[Unit] = {
    var mobile = phoneNumber.toString
    if (mobile.length > 11) {
      mobile = mobile.substring(mobile.length - 11)
    }
    system.log.error("=======================TelesignSmsEngine sendCode phoneNumber:{} message:{}", mobile, message)
    val body = List(
      "smsMob" → URLEncoder.encode(mobile, Utf8Encoding),
      "Uid" → customerId,
      "Key" → apiKey,
      "smsText" → URLEncoder.encode("验证码:" + message, Utf8Encoding)
    ).map(p ⇒ s"${p._1}=${p._2}").mkString("&")

    val request = (resourceUrl.POST.setContentType(FormContentType, Utf8Encoding).setBody(body))

    val f = http(request)

    f onComplete {
      case Success(v) ⇒
        if (v.getStatusCode > 199 && v.getStatusCode < 300) {
          system.log.error("Message sent to webchinese")
        } else {
          system.log.error(s"webchinese replied with error ${v.getResponseBody(Utf8Encoding)}")
        }
      case Failure(e) ⇒
        system.log.error(e, "Failed to send sms through webchinese")
    }

    f map (_ ⇒ ())
  }
}
