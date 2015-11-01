package im.actor.server.api.http.bots

import java.util.Base64

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import cats.data.OptionT
import cats.std.future._
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import im.actor.api.rpc.messaging.ApiJsonMessage
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.api.rpc.sequence.UpdateRawUpdate
import im.actor.server.api.http.RoutesHandler
import im.actor.server.api.http.json.{ ContentUnmarshaller, JsValueUnmarshaller, JsonFormatters, Status }
import im.actor.server.bot.{ BotExtension, BotServerBlueprint }
import im.actor.server.dialog.DialogExtension
import im.actor.server.sequence.SeqUpdatesManager
import im.actor.server.user.UserExtension
import play.api.libs.json._
import upickle.default._

import scala.concurrent.Future
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.util.control.NoStackTrace
import scala.util.{ Failure, Success }

private[http] final class BotsHandler(implicit system: ActorSystem, val materializer: Materializer) extends RoutesHandler with PlayJsonSupport with JsValueUnmarshaller with ContentUnmarshaller {

  import JsonFormatters._
  import im.actor.bots.BotMessages.{ BotRequest, BotResponse, BotUpdate }
  import system._

  private val botExt = BotExtension(system)
  private val userExt = UserExtension(system)

  override def routes: Route =
    path("bots" / "hooks" / Segment) { token ⇒
      post {
        entity(as[String]) { data ⇒
          extractRequest { request ⇒
            val headers = request.headers.map(header ⇒ header.name() → header.value())
            onComplete(sendMessage(headers, data, token)) {
              case Success(result) ⇒
                result match {
                  case Left(statusCode) ⇒ complete(statusCode → Status("failure"))
                  case Right(_)         ⇒ complete(OK → Status("ok"))
                }
              case Failure(e) ⇒
                log.error(e, "Failed to handle bot hook")
                complete(InternalServerError)
            }
          }
        }
      }
    } ~ path("bots" / Segment) { token ⇒
      val flowFuture = (for {
        userId ← OptionT(botExt.findUserId(token))
        authId ← OptionT(botExt.findAuthId(token))
      } yield flow(userId, authId)).value map {
        case Some(r) ⇒ r
        case None ⇒
          val e = new RuntimeException("Wrong token") with NoStackTrace
          log.error(e.getMessage)
          throw e
      }

      onSuccess(flowFuture) {
        case flow ⇒ handleWebsocketMessages(flow)
      }
    }

  private def sendMessage(headers: Seq[(String, String)], data: String, token: String): Future[Either[StatusCode, Unit]] = {
    (for {
      hook ← OptionT(botExt.findWebHook(token))
      _ ← OptionT.pure(userExt.broadcastUserUpdate(
        userId = hook.userId,
        update = UpdateRawUpdate(
          `type` = Some("HookData"),
          bytes = JsObject(Map(
            "dataType" → JsString("HookData"),
            "data" → JsObject(Map(
              "name" → JsString(hook.name),
              "headers" → JsObject(headers map { case (name, value) ⇒ name → JsString(value) }),
              "body" → JsString(Base64.getEncoder.encodeToString(data.getBytes("UTF-8")))
            ))
          )).toString().getBytes("UTF-8")
        ),
        pushText = None,
        isFat = false,
        deliveryId = None
      ))
    } yield Right(())).value map {
      case Some(r) ⇒ r
      case None ⇒
        val e = new RuntimeException("Wrong token") with NoStackTrace
        log.error(e.getMessage)
        throw e
    }
  }

  private def flow(botUserId: Int, botAuthId: Long) = {
    val bp = new BotServerBlueprint(botUserId, botAuthId, system)

    Flow[Message]
      .collect {
        case TextMessage.Strict(text) ⇒
          val rq = read[BotRequest](text)
          log.debug("Bot request: {}, userId: {}", rq, botUserId)
          rq
        case tm: TextMessage ⇒ throw new RuntimeException("Streamed text message is not supported") with NoStackTrace
      }
      .via(bp.flow)
      .map {
        case rsp: BotResponse ⇒
          log.debug("Bot response {}", rsp)
          write[BotResponse](rsp)
        case upd: BotUpdate ⇒
          log.debug("Bot update {}", upd)
          write[BotUpdate](upd)
      }
      .map(TextMessage.Strict(_).asInstanceOf[Message])
  } recover {
    case e ⇒
      log.error(e, "Failure in websocket bot stream, userId: {}", botUserId)
      throw e
  }
}