package im.actor.server.sms

import java.net.URLEncoder

import akka.actor.ActorSystem
import com.ning.http.client.Response
import com.typesafe.config.Config
import dispatch.{ Http, url }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Failure

final class MagfaClient(config: Config)(implicit system: ActorSystem) {

  private lazy val http = new Http()
  private val Utf8Encoding = "UTF-8"
  system registerOnTermination http.shutdown()

  private implicit val ec: ExecutionContext = system.dispatcher

  private val BaseUrl = config.getString("url")
  //  private val ResourcePath = config.getString("/services/send")
  private val ResourcePath = "/services/send?"

  def sendSmsCode(phoneNumber: Long, code: String): Future[Unit] = {
    postRequest(ResourcePath, Map(
      "from" → config.getString("from"),
      "to" → phoneNumber.toString,
      "message" → code
    )) map { _ ⇒
      system.log.debug("Message sent via magfa")
    }
  }

  private def postRequest(resourcePath: String, params: Map[String, String]): Future[Response] = {
    val body = params.map(p ⇒ s"${p._1}=${URLEncoder.encode(p._2, Utf8Encoding)}").mkString("&")
    val request = url(BaseUrl + resourcePath + body)

    http(request).map { resp ⇒
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