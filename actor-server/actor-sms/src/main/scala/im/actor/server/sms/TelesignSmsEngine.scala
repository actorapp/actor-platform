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
  private val apiKey = Base64.getDecoder.decode(config.getString("api-key"))
  private val customerId = config.getString("customer-id")
  private val resourceUriString = "/v1/verify/sms"
  private val resourceUrl = url("https://rest.telesign.com" + resourceUriString)
  private val dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z")

  private val HmacSha1Algorithm = "HmacSHA1"
  private val Utf8Encoding = "UTF-8"
  val FormContentType = "application/x-www-form-urlencoded"

  private val http = new Http()

  implicit val ec: ExecutionContext = system.dispatcher

  override def sendCode(phoneNumber: Long, message: String): Future[Unit] = {
    val body = List(
      "phone_number" → URLEncoder.encode(phoneNumber.toString, Utf8Encoding),
      "ucid" → "BACS",
      "verify_code" → URLEncoder.encode(message, Utf8Encoding),
      "template" → URLEncoder.encode("$$CODE$$ is your Actor code", Utf8Encoding)
    ).map(p ⇒ s"${p._1}=${p._2}").mkString("&")

    val request = (resourceUrl.POST.setContentType(FormContentType, Utf8Encoding).setBody(body))

    val date = dateFormat.format(new Date)

    val stringToSign =
      s"""POST
        |${FormContentType}; charset=${Utf8Encoding}
        |${date}
        |${body}
        |${resourceUriString}""".stripMargin

    val mac = Mac.getInstance(HmacSha1Algorithm)
    mac.init(new SecretKeySpec(apiKey, HmacSha1Algorithm))
    val hmac = mac.doFinal(stringToSign.getBytes(Utf8Encoding))

    val signature = Base64.getEncoder.encodeToString(hmac)

    val authValue = s"TSA ${customerId}:${signature}"

    val requestWithAuth = request
      .addHeader("Date", date)
      .addHeader("Authorization", authValue)

    val f = http(requestWithAuth)

    f onComplete {
      case Success(v) ⇒
        if (v.getStatusCode > 199 && v.getStatusCode < 300) {
          system.log.debug("Message sent to Telesign")
        } else {
          system.log.error(s"Telesign replied with error ${v.getResponseBody(Utf8Encoding)}")
        }
      case Failure(e) ⇒
        system.log.error(e, "Failed to send sms through Telesign")
    }

    f map (_ ⇒ ())
  }
}
