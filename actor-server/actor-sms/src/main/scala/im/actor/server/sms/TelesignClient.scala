package im.actor.server.sms

import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.{ Base64, Date }
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import akka.actor.ActorSystem
import com.ning.http.client.Response
import com.typesafe.config.Config
import dispatch.{ Http, Req, url }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Failure

object TelesignClient {
  private val BaseUrl = "https://rest.telesign.com"
  private val ApiVersion = "v1"
  val DefaultSmsTemplate: String = "$$SERVER_NAME$$ activation code: $$CODE$$"
  val DefaultLanguage = "en"
  val CallLanguages: Set[String] = "af sq ar ar-EG hy bn eu be bn-BD bs bg ca ku zh-HK zh-CN zh-TW hr cs da fa-AF nl egx en-AU en-GB en-US et fil fi fr fr-CA gl ka de el gu ha he hi hu is id zu it ja kn kk km sw ko ko ky lv ln lt lb mk ms ml mr ne no or fa pl pt pt-BR pt-PT pa-IN pa-PK ro ru sr sr-BA sd sk sk-SK sl es es-ES es-CL es-419 es-ES sv gsw ta te th tr uk ur vi cy".split(" ").toSet
}

final class TelesignClient(config: Config)(implicit system: ActorSystem) {

  import TelesignClient._

  private val apiKey = Base64.getDecoder.decode(config.getString("api-key"))
  private val customerId = config.getString("customer-id")
  private val dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z")

  private val HmacSha1Algorithm = "HmacSHA1"
  private val Utf8Encoding = "UTF-8"
  private val FormContentType = "application/x-www-form-urlencoded"
  private lazy val http = new Http()

  system registerOnTermination http.shutdown()

  private implicit val ec: ExecutionContext = system.dispatcher

  def sendSmsCode(phoneNumber: Long, code: String, template: String): Future[Unit] = {
    postRequest("/verify/sms", Map(
      "phone_number" → phoneNumber.toString,
      "ucid" → "BACS",
      "verify_code" → code,
      "template" → template.replace("$$SERVER_NAME$$", system.settings.config.getString("name"))
    )) map { _ ⇒
      system.log.debug("Message sent via telesign")
    }
  }

  def sendCallCode(phoneNumber: Long, code: String, language: String): Future[Unit] = {
    postRequest("/verify/call", Map(
      "phone_number" → phoneNumber.toString,
      "ucid" → "BACS",
      "verify_code" → code,
      "language" → (if (CallLanguages.exists(_ == language)) language else DefaultLanguage).toLowerCase
    )) map { _ ⇒
      system.log.debug("Call sent via telesign")
    }
  }

  private def resourceUrl(resourcePath: String): Req = url(s"$BaseUrl${resourceUri(resourcePath)}")
  private def resourceUri(resourcePath: String): String = s"/$ApiVersion$resourcePath"

  private def postRequest(resourcePath: String, params: Map[String, String]): Future[Response] = {
    val body = params.map(p ⇒ s"${p._1}=${URLEncoder.encode(p._2, Utf8Encoding)}").mkString("&")
    val resUrl = resourceUrl(resourcePath)
    val request = (resUrl.POST.setContentType(FormContentType, Utf8Encoding).setBody(body))
    val date = dateFormat.format(new Date)
    val stringToSign =
      s"""POST
          |${FormContentType}; charset=${Utf8Encoding}
          |${date}
          |${body}
          |${resourceUri(resourcePath)}""".stripMargin

    val mac = Mac.getInstance(HmacSha1Algorithm)
    mac.init(new SecretKeySpec(apiKey, HmacSha1Algorithm))
    val hmac = mac.doFinal(stringToSign.getBytes(Utf8Encoding))

    val signature = Base64.getEncoder.encodeToString(hmac)

    val authValue = s"TSA ${customerId}:${signature}"

    val requestWithAuth = request
      .addHeader("Date", date)
      .addHeader("Authorization", authValue)

    http(requestWithAuth).map { resp ⇒
      if (resp.getStatusCode < 199 || resp.getStatusCode > 299) {
        throw new RuntimeException(s"Response has code ${resp.getStatusCode}. [${resp.getResponseBody}]")
      } else {
        resp
      }
    } andThen {
      case Failure(e) ⇒
        system.log.error(e, "Failed to make request to telesign")
    }
  }
}