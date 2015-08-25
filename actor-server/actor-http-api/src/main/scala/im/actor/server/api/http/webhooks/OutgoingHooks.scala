package im.actor.server.api.http.webhooks

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ HttpResponse, StatusCode, Uri }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.data.Xor
import im.actor.server.api.http.json.{ ReverseHookUnmarshaler, JsonFormatters, ReverseHook }
import im.actor.server.commons.KeyValueMappings
import im.actor.server.group.GroupOffice
import im.actor.server.util.FutureResult
import shardakka.ShardakkaExtension
import shardakka.keyvalue.SimpleKeyValue
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport

import scala.concurrent.Future
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.util.Try

object FutureResultHttp extends FutureResult[(StatusCode, String)]

object OutgoingHooksErrors {
  val WrongIntegrationToken = "Wrong integration token"
  val NotAllowedForPublic = "Reverse webhooks for public group are not allowed"
  val MalformedUri = "Malformed outgoing hook uri"
  val AlreadyRegistered = "Webhooks with provided uri is already registered"
}

trait OutgoingHooks extends ReverseHookUnmarshaler {
  self: WebhooksHandler ⇒

  import JsonFormatters._
  import PlayJsonSupport._
  import FutureResultHttp._

  def outgoingRoutes: Route = path("reverse" / Segment) { token ⇒
    post {
      entity(as[ReverseHook]) { hook ⇒
        onSuccess(register(token, hook.url)) {
          case Xor.Left((status, message)) ⇒ complete(HttpResponse(status, entity = message))
          case Xor.Right(_)                ⇒ complete(HttpResponse(OK, entity = s"Successfully registered reverse hook on ${hook.url}"))
        }
      }
    } ~ get {
      onSuccess(list(token)) {
        case Xor.Left((status, message)) ⇒ complete(HttpResponse(status, entity = message))
        case Xor.Right(hooks)            ⇒ complete(hooks map ReverseHook)
      }
    }
  }

  def register(token: String, uri: String): Future[(StatusCode, String) Xor Unit] = {
    (for {
      groupId ← fromFutureOption(BadRequest → OutgoingHooksErrors.WrongIntegrationToken)(integrationTokensKv.get(token))
      uri ← fromXor(e ⇒ BadRequest → OutgoingHooksErrors.MalformedUri)(Xor.fromTry(Try(Uri(uri))))
      strUri = uri.toString()

      reverseHooksKv = getTokenKv(token)

      _ ← fromFutureBoolean(Forbidden → OutgoingHooksErrors.NotAllowedForPublic)(GroupOffice.isPublic(groupId) map (!_))

      registeredUrs ← fromFuture(getHookUrls(token))
      _ ← fromBoolean(NotAcceptable → OutgoingHooksErrors.AlreadyRegistered)(!registeredUrs.contains(strUri))

      result ← fromFuture(reverseHooksKv.upsert(ThreadLocalRandom.current().nextLong().toString, strUri))
    } yield result).value
  }

  def list(token: String): Future[(StatusCode, String) Xor Seq[String]] = {
    (for {
      groupId ← fromFutureOption(BadRequest → OutgoingHooksErrors.WrongIntegrationToken)(integrationTokensKv.get(token))

      _ ← fromFutureBoolean(Forbidden → OutgoingHooksErrors.NotAllowedForPublic)(GroupOffice.isPublic(groupId) map (!_))

      result ← fromFuture(getHookUrls(token))
    } yield result).value
  }

  private def getTokenKv(token: String): SimpleKeyValue[String] =
    ShardakkaExtension(system).simpleKeyValue(KeyValueMappings.ReverseHooks + "_" + token)

  private def getHookUrls(token: String): Future[Seq[String]] = {
    val kv = getTokenKv(token)
    for {
      keys ← kv.getKeys()
      values ← Future.sequence(keys map kv.get) map (_.flatten)
    } yield values
  }

}
