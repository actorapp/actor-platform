package im.actor.server.sms

import scala.concurrent.{ ExecutionContext, Future }

import akka.actor._
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.FlowMaterializer
import com.typesafe.config._

class ClickatellSmsEngine(config: Config)(implicit system: ActorSystem, flowMaterializer: FlowMaterializer, http: HttpExt) extends SmsEngine {
  private val user = config.getString("user")
  private val password = config.getString("password")
  private val apiId = config.getString("api-id")
  private val baseUri = Uri("http://api.clickatell.com/http/sendmsg")

  implicit val ec: ExecutionContext = system.dispatcher

  override def send(phoneNumber: Long, message: String): Future[Unit] = {
    val uri = baseUri.withQuery(Map(
      "user" → user,
      "password" → password,
      "api_id" → apiId,
      "to" → phoneNumber.toString,
      "text" → utfToHexString(message),
      "unicode" → 1.toString
    ))

    val request = HttpRequest(uri = uri)

    val f = http.singleRequest(request) map {
      case HttpResponse(StatusCodes.OK, _, entity, _) ⇒
        // FIXME: check if body starts with OK
        ()
      case resp ⇒
        throw new Exception(s"Wrong response: ${resp}")
    }

    f onFailure {
      case e ⇒
        system.log.error(e, "Failed to send sms to clickatell")
    }

    f
  }

  private def utfToHexString(from: String) = { from.map(ch ⇒ f"${ch.toInt}%04X").mkString }
}
