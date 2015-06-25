package im.actor.server.sms

import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{ Authorization, BasicHttpCredentials }
import akka.stream.Materializer
import com.typesafe.config.Config

class TwilioSmsEngine(config: Config)(implicit system: ActorSystem, materializer: Materializer, http: HttpExt) extends SmsEngine {

  private val account = config.getString("account")
  private val token = config.getString("token")
  private val from = config.getString("from")

  implicit val ec: ExecutionContext = system.dispatcher

  private val baseUri = Uri(s"https://api.twilio.com/2010-04-01/Accounts/$account/Messages.json")

  private val authHeader = Authorization(BasicHttpCredentials(account, token))

  override def send(phoneNumber: Long, message: String): Future[Unit] = {
    val to = s"+${phoneNumber}"

    Marshal(FormData(Map("From" → from, "To" → to, "Body" → message))).to[RequestEntity] flatMap { entity ⇒
      val request = HttpRequest(
        method = HttpMethods.POST,
        uri = baseUri,
        entity = entity
      ).withHeaders(authHeader)

      http.outgoingConnection("api.twilio.com", 443)

      val f = http.singleRequest(request) map {
        case HttpResponse(StatusCodes.Created, _, _, _) ⇒ ()
        case resp ⇒
          throw new Exception(s"Wrong response: ${resp}")
      }

      f onFailure {
        case e ⇒
          system.log.error(e, "Failed to send sms through twilio")
      }

      f
    }
  }
}
