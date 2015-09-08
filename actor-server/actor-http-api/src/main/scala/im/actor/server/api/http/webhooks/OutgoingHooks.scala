package im.actor.server.api.http.webhooks

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ StatusCode, Uri }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.data.Xor
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import im.actor.server.KeyValueMappings
import im.actor.server.api.http.json._
import im.actor.concurrent.FutureResultCats
import im.actor.util.misc.IdUtils
import shardakka.ShardakkaExtension
import shardakka.keyvalue.SimpleKeyValue

import scala.concurrent.Future
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.util.Try

object FutureResultHttp extends FutureResultCats[(StatusCode, String)]

object OutgoingHooksErrors {
  val WrongIntegrationToken = "Wrong integration token"
  val MalformedUri = "Malformed outgoing hook uri"
  val AlreadyRegistered = "Webhooks with provided uri is already registered"
  val WebhookGone = "Webhook with given id not found"
}

trait OutgoingHooks extends ReverseHookUnmarshaler with PlayJsonSupport {
  self: WebhooksHandler ⇒

  import FutureResultHttp._
  import JsonFormatters._

  // format: OFF
  def outgoing: Route =
    path(Segment / "reverse") { token ⇒
      post {
        entity(as[ReverseHook]) { hook ⇒
          onSuccess(register(token, hook.url)) {
            case Xor.Left((status, message)) ⇒ complete(status → Errors(message))
            case Xor.Right(id) ⇒ complete(Created → ReverseHookResponse(id, None))
          }
        }
      } ~
      get {
        onSuccess(list(token)) {
          case Xor.Left((status, message)) ⇒ complete(status → Errors(message))
          case Xor.Right(hooks) ⇒ complete(hooks)
        }
      }
    } ~
    path(Segment / "reverse" / IntNumber) { (token, id) ⇒
      get {
        onSuccess(findHook(token, id)) {
          case Some(_) ⇒ complete(OK → Status("Ok"))
          case None    ⇒ complete(Gone → Status(OutgoingHooksErrors.WebhookGone))
        }
      } ~
      delete {
        onSuccess(unregister(token, id)) {
          case Xor.Left((status, message)) ⇒ complete(status → Errors(message))
          case Xor.Right(_)                ⇒ complete(Accepted → Status("Ok"))
        }
      }
    }
  // format: ON

  def findHook(token: String, id: Int): Future[Option[Int]] = {
    for {
      idToUrls ← getHooks(token)
    } yield idToUrls.map(_._1).find(_ == id)
  }

  def register(token: String, uri: String): Future[(StatusCode, String) Xor Int] = {
    (for {
      groupId ← fromFutureOption(NotFound → OutgoingHooksErrors.WrongIntegrationToken)(integrationTokensKv.get(token))
      uri ← fromXor(e ⇒ BadRequest → OutgoingHooksErrors.MalformedUri)(Xor.fromTry(Try(Uri(uri))))
      strUri = uri.toString()

      registeredUrs ← fromFuture(getHooks(token))
      _ ← fromBoolean(Conflict → OutgoingHooksErrors.AlreadyRegistered)(!registeredUrs.map(_._2).contains(strUri))

      id = IdUtils.nextIntId(ThreadLocalRandom.current())
      _ ← fromFuture(getTokenKv(token).upsert(id.toString, strUri))
    } yield id).value
  }

  def unregister(token: String, id: Int): Future[(StatusCode, String) Xor Unit] = {
    (for {
      groupId ← fromFutureOption(NotFound → OutgoingHooksErrors.WrongIntegrationToken)(integrationTokensKv.get(token))
      _ ← fromFutureOption(Gone → OutgoingHooksErrors.WebhookGone)(findHook(token, id))
      _ ← fromFuture(getTokenKv(token).delete(id.toString))
    } yield ()).value
  }

  def list(token: String): Future[(StatusCode, String) Xor Seq[ReverseHookResponse]] = {
    (for {
      groupId ← fromFutureOption(NotFound → OutgoingHooksErrors.WrongIntegrationToken)(integrationTokensKv.get(token))
      hooks ← fromFuture(getHooks(token))
      result = hooks.map(h ⇒ ReverseHookResponse(h._1, Some(h._2)))
    } yield result).value
  }

  private def getTokenKv(token: String): SimpleKeyValue[String] =
    ShardakkaExtension(system).simpleKeyValue(KeyValueMappings.ReverseHooks + "_" + token)

  private def getHooks(token: String): Future[Seq[(Int, String)]] = {
    val kv = getTokenKv(token)
    for {
      keys ← kv.getKeys()
      idToUrls = keys map { key ⇒ kv.get(key) map (_.map(key.toInt → _)) }
      values ← Future.sequence(idToUrls) map (_.flatten)
    } yield values
  }

}
