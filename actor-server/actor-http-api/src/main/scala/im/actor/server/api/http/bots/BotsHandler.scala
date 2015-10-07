package im.actor.server.api.http.bots

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Flow
import cats.data.OptionT
import cats.std.future._
import im.actor.bots.BotMessages.{ BotRequest, BotResponse, BotSeqUpdate }
import im.actor.server.api.http.RoutesHandler
import im.actor.server.bot.{ BotServerBlueprint, BotExtension }
import upickle.default._

import scala.util.control.NoStackTrace

private[http] final class BotsHandler(implicit system: ActorSystem) extends RoutesHandler {

  import system._

  private val botExt = BotExtension(system)

  override def routes: Route = path("bots" / Segment) { token ⇒
    val flowFuture = (for {
      userId ← OptionT(botExt.getUserId(token))
      authId ← OptionT(botExt.getAuthId(token))
    } yield flow(userId, authId)).value map {
      case Some(r) ⇒ r
      case None ⇒
        throw new RuntimeException("Wrong token") with NoStackTrace
    }

    onSuccess(flowFuture) {
      case flow ⇒ handleWebsocketMessages(flow)
    }
  }

  private def flow(botUserId: Int, botAuthId: Long) = {
    val bp = new BotServerBlueprint(botUserId, botAuthId, system)

    Flow[Message]
      .collect {
        case TextMessage.Strict(text) ⇒ read[BotRequest](text)
        case tm: TextMessage          ⇒ throw new RuntimeException("Streamed text message is not supported") with NoStackTrace
      }
      .via(bp.flow)
      .map {
        case rsp: BotResponse ⇒
          log.debug("Sending response {}", rsp)
          write[BotResponse](rsp)
        case upd: BotSeqUpdate ⇒
          log.debug("Sending update {}", upd)
          write[BotSeqUpdate](upd)
        case unmatched ⇒
          log.error("Unmatched {}", unmatched)
          throw new RuntimeException(s"Unmatched BotMessage ${unmatched}")
      }
      .map(TextMessage.Strict(_).asInstanceOf[Message])
  }
}