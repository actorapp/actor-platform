package im.actor.server.bot

import im.actor.bots.BotMessages
import upickle.Js
import upickle.default._

import scala.concurrent.{ ExecutionContext, Future }

object BotService {
  import BotMessages._

  type BotUserId = Int
  type BotAuthId = Long

  type RequestResult[+RSP <: ResponseBody] = Either[BotError, RSP]

  private type Handler[+RSP <: ResponseBody] = (BotUserId, BotAuthId) ⇒ Future[RequestResult[RSP]]

  case class RequestHandler[+RQ <: RequestBody, RSP <: ResponseBody: Writer](handle: Handler[RQ#Response]) {
    def result(botUserId: Int, botAuthId: Long)(implicit ec: ExecutionContext): Future[BotResponseBody] =
      for {
        res ← handle(botUserId, botAuthId)
      } yield res match {
        case Right(rsp)  ⇒ BotSuccess(writeJs(rsp.asInstanceOf[RSP]).asInstanceOf[Js.Obj])
        case Left(error) ⇒ error
      }

    def toWeak(implicit ec: ExecutionContext) = WeakRequestHandler(
      (botUserId: Int, botAuthId: Long) ⇒
        result(botUserId, botAuthId)
    )
  }

  case class WeakRequestHandler(handle: (BotUserId, BotAuthId) ⇒ Future[BotResponseBody])
}

trait BotService {
  import BotMessages._

  import BotService._

  def handlers: PartialFunction[RequestBody, WeakRequestHandler]
}