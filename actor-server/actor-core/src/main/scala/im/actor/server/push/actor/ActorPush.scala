package im.actor.server.push.actor

import akka.NotUsed
import akka.actor._
import akka.event.Logging
import akka.stream.{ ActorMaterializer, OverflowStrategy }
import akka.stream.scaladsl.{ Flow, Sink, Source }
import im.actor.server.db.DbExtension
import im.actor.server.model.push.ActorPushCredentials
import im.actor.server.persist.push.ActorPushCredentialsRepo
import io.circe.{ Json, JsonObject }
import io.circe.generic.auto._
import io.circe.syntax._
import spray.client.pipelining._
import spray.http.HttpHeaders.Authorization
import spray.http._
import spray.http.parser.ParserInput

import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }

final case class ActorPushMessage(data: JsonObject)

object ActorPushMessage {
  def apply(fields: Map[String, String]): ActorPushMessage =
    ActorPushMessage(JsonObject.fromMap(fields mapValues Json.string))

  def apply(fields: (String, String)*): ActorPushMessage =
    ActorPushMessage(Map(fields: _*))
}

private final case class ActorPushDelivery(creds: ActorPushCredentials, message: ActorPushMessage)

final class ActorPush(system: ActorSystem) extends Extension {
  import system.dispatcher

  private implicit val _system = system
  private val log = Logging(system, getClass)
  private val maxQueue = system.settings.config.getInt("services.actor.push.max-queue")
  private val token = system.settings.config.getString("services.actor.push.token")
  private implicit val mat = ActorMaterializer()
  private val db = DbExtension(system).db

  private val sourceRef =
    Source
      .actorRef[(HttpRequest, ActorPushDelivery)](maxQueue, OverflowStrategy.dropHead)
      .via(flow)
      .to(Sink foreach {
        case (Success(_), d) ⇒
        case (Failure(e), d) ⇒ log.error(e, "Failed to deliver, endpoint: {}", d.creds.endpoint)
      })
      .run()

  private val pushHeaders = List(Authorization(OAuth2BearerToken(token)))

  private def flow: Flow[(HttpRequest, ActorPushDelivery), (Try[HttpResponse], ActorPushDelivery), NotUsed] = {
    import system.dispatcher
    val pipeline = sendReceive
    Flow[(HttpRequest, ActorPushDelivery)].mapAsync(1) {
      case (req, del) ⇒
        pipeline(req) map { resp ⇒
          if (resp.status.isSuccess) {
            log.debug("Successfully delivered message: {}", del)
            Success(resp) → del
          } else {
            Failure(new RuntimeException(s"Failed to deliver message, StatusCode was not OK: ${resp.status}")) → del
          }
        }
    }
  }

  def deliver(seq: Int, creds: ActorPushCredentials): Unit =
    deliver(ActorPushMessage(JsonObject.singleton("seq", Json.int(seq))), creds)

  def deliver(message: ActorPushMessage, creds: ActorPushCredentials): Unit = {
    val uri = Uri.parseAbsolute(ParserInput(creds.endpoint))

    sourceRef !
      HttpRequest(
        method = HttpMethods.POST,
        uri = uri,
        headers = pushHeaders,
        entity = HttpEntity(ContentTypes.`application/json`, message.asJson.noSpaces)
      ) → ActorPushDelivery(creds, message)
  }

  def fetchCreds(userId: Int): Future[Seq[ActorPushCredentials]] =
    db.run(ActorPushCredentialsRepo.findByUser(userId))
}

object ActorPush extends ExtensionId[ActorPush] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): ActorPush = new ActorPush(system)

  override def lookup(): ExtensionId[_ <: Extension] = ActorPush
}