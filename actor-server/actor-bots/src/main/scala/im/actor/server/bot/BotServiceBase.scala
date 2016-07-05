package im.actor.server.bot

import akka.actor.ActorSystem
import akka.http.scaladsl.util.FastFuture
import im.actor.bots.BotMessages
import im.actor.concurrent.XorEitherConversions
import im.actor.server.user.UserExtension
import upickle.Js
import upickle.default._

import scala.concurrent.{ ExecutionContext, Future }

object BotServiceTypes extends BotServiceTypes

trait BotServiceTypes extends BotToInternalConversions {
  import BotMessages._

  type BotUserId = Int
  type BotAuthId = Long
  type BotAuthSid = Int

  type RequestResult[+RSP <: ResponseBody] = Either[BotError, RSP]

  private type Handler[+RSP <: ResponseBody] = (BotUserId, BotAuthId, BotAuthSid) ⇒ Future[RequestResult[RSP]]

  case class RequestHandler[+RQ <: RequestBody, RSP <: ResponseBody: Writer](handle: Handler[RQ#Response]) {
    def result(botUserId: Int, botAuthId: Long, botAuthSid: Int)(implicit ec: ExecutionContext): Future[BotResponseBody] =
      for {
        res ← handle(botUserId, botAuthId, botAuthSid)
      } yield res match {
        case Right(rsp)  ⇒ BotSuccess(writeJs(rsp.asInstanceOf[RSP]).asInstanceOf[Js.Obj])
        case Left(error) ⇒ error
      }

    def toWeak(implicit ec: ExecutionContext) = WeakRequestHandler(
      (botUserId: Int, botAuthId: Long, botAuthSid: Int) ⇒
        result(botUserId, botAuthId, botAuthSid)
    )
  }

  case class WeakRequestHandler(handle: (BotUserId, BotAuthId, BotAuthSid) ⇒ Future[BotResponseBody])
}

abstract class BotServiceBase(system: ActorSystem) extends BotServiceTypes with XorEitherConversions {
  import BotMessages._
  import system.dispatcher

  type Handlers = PartialFunction[RequestBody, WeakRequestHandler]

  def handlers: Handlers

  val userExt = UserExtension(system)

  protected def ifIsAdmin[R <: ResponseBody](userId: BotUserId)(f: Future[RequestResult[R]]): Future[RequestResult[R]] = {
    userExt.isAdmin(userId) flatMap { isAdmin ⇒
      if (isAdmin)
        f
      else
        FastFuture.successful(Left(BotError(403, "FORBIDDEN")))
    }
  }
}
