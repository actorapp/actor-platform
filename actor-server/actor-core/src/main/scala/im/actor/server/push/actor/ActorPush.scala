package im.actor.server.push.actor

import akka.actor._
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.parboiled2.ParserInput
import akka.stream.{ ActorMaterializer, OverflowStrategy }
import akka.stream.scaladsl.{ Sink, Source }
import im.actor.server.model.push.ActorPushCredentials
import io.circe.{ Json, JsonObject }
import io.circe.generic.auto._
import io.circe.syntax._

import scala.util.{ Failure, Success }

final case class ActorPushMessage(data: JsonObject)

private final case class ActorPushDelivery(creds: ActorPushCredentials, message: ActorPushMessage)

final class ActorPush(_system: ActorSystem) extends Extension {
  private implicit val system = _system
  private val log = Logging(system, getClass)
  private val maxQueue = system.settings.config.getInt("services.actor.push.max-queue")
  private val token = system.settings.config.getString("services.actor.push.token")
  private implicit val mat = ActorMaterializer()

  private val sourceRef =
    Source
      .actorRef[(HttpRequest, ActorPushDelivery)](maxQueue, OverflowStrategy.dropHead)
      .via(Http(system).superPool[ActorPushDelivery]())
      .to(Sink foreach {
        case (Success(_), d) ⇒
        case (Failure(e), d) ⇒ log.error(e, "Failed to deliver, endpoint: {}", d.creds.endpoint)
      })
      .run()

  private val pushHeaders = List(headers.Authorization(headers.OAuth2BearerToken(token)))

  def deliver(seq: Int, creds: ActorPushCredentials): Unit = {
    val m = ActorPushMessage(JsonObject.singleton("seq", Json.int(seq)))
    val uri = Uri.parseAbsolute(ParserInput(creds.endpoint))

    sourceRef !
      HttpRequest(
        method = HttpMethods.POST,
        uri = uri,
        headers = pushHeaders,
        entity = HttpEntity(ContentTypes.`application/json`, m.asJson.noSpaces)
      ) → ActorPushDelivery(creds, m)
  }
}

object ActorPush extends ExtensionId[ActorPush] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): ActorPush = new ActorPush(system)

  override def lookup(): ExtensionId[_ <: Extension] = ActorPush
}