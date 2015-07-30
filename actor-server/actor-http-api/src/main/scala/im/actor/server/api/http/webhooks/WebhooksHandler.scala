package im.actor.server.api.http.webhooks

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ HttpResponse, StatusCode, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.util.Timeout
import im.actor.api.rpc.messaging.{ Message, TextMessage }
import im.actor.server.api.http.RoutesHandler
import im.actor.server.api.http.json._
import im.actor.server.peermanagers.{ GroupPeerManager, GroupPeerManagerRegion }
import im.actor.server.persist
import org.joda.time.DateTime
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

class WebhooksHandler()(
  implicit
  db:                     Database,
  ec:                     ExecutionContext,
  groupPeerManagerRegion: GroupPeerManagerRegion,
  val materializer:       Materializer
) extends RoutesHandler with ContentUnmarshaler {

  implicit val timeout: Timeout = Timeout(5.seconds)

  override def routes: Route = path("webhooks" / Segment) { token ⇒
    post {
      entity(as[Content]) { content ⇒
        onComplete(send(content, token)) {
          case Success(result) ⇒
            result match {
              case Left(statusCode) ⇒ complete(statusCode)
              case Right(_)         ⇒ complete(HttpResponse(OK))
            }
          case Failure(e) ⇒ complete(HttpResponse(InternalServerError))
        }
      }
    }
  }

  def send(content: Content, token: String): Future[Either[StatusCode, Unit]] = {
    val message: Message = content match {
      case Text(text)    ⇒ TextMessage(text, Vector.empty, None)
      case Document(url) ⇒ throw new NotImplementedError()
      case Image(url)    ⇒ throw new NotImplementedError()
    }

    val action: DBIO[Either[ClientError, Unit]] = for {
      optBot ← persist.GroupBot.findByToken(token)
      result ← optBot.map { bot ⇒
        for {
          optGroup ← persist.Group.find(bot.groupId)
          authIds ← persist.AuthId.findByUserId(bot.userId)
          authId ← (optGroup, authIds) match {
            case (None, _)                          ⇒ DBIO.successful(Left(StatusCodes.NotFound))
            case (Some(group), _) if group.isPublic ⇒ DBIO.successful(Left(StatusCodes.Forbidden))
            case (Some(group), auth +: _) ⇒
              val sendFuture = for {
                _ ← GroupPeerManager.sendMessage(group.id, bot.userId, auth.id, ThreadLocalRandom.current().nextLong(), DateTime.now, message)
              } yield Right(())
              DBIO.from(sendFuture)
            case (Some(group), Seq()) ⇒
              val rng = ThreadLocalRandom.current()
              val authId = rng.nextLong()
              for {
                _ ← persist.AuthId.create(authId, Some(bot.userId), None)
                _ ← DBIO.from(GroupPeerManager.sendMessage(group.id, bot.userId, authId, rng.nextLong(), DateTime.now, message))
              } yield Right(())
          }
        } yield authId
      } getOrElse DBIO.successful(Left(StatusCodes.BadRequest))
    } yield result
    db.run(action)
  }

}
