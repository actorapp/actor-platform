package im.actor.server.sms

import scala.concurrent.{ ExecutionContext, Future }

import akka.actor._
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.typesafe.config._

import im.actor.util.misc.StringUtils._

class ClickatellSmsEngine(config: Config)(implicit system: ActorSystem, materializer: Materializer, http: HttpExt) extends SmsEngine {
  private val user = config.getString("user")
  private val password = config.getString("password")
  private val apiId = config.getString("api-id")
  private val baseUri = Uri("http://api.clickatell.com/http/sendmsg")

  private val baseParams = Map(
    "user" → user,
    "password" → password,
    "api_id" → apiId
  )

  implicit val ec: ExecutionContext = system.dispatcher

  override def send(phoneNumber: Long, message: String): Future[Unit] = {
    val params = baseParams + ("to" → phoneNumber.toString)

    val uri = if (isAsciiString(message))
      baseUri.withQuery(params + ("text" → message))
    else
      baseUri.withQuery(params ++ Map(
        "text" → utfToHexString(message),
        "unicode" → 1.toString
      ))

    val f = http.singleRequest(HttpRequest(uri = uri)) map {
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

}
