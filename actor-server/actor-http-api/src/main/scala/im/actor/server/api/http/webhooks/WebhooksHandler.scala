package im.actor.server.api.http.webhooks

import im.actor.server.group.{ GroupViewRegion, GroupOffice, GroupProcessorRegion }

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ HttpResponse, StatusCode, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.util.Timeout
import im.actor.api.rpc.messaging.{ Message, TextMessage }
import im.actor.server.api.http.RoutesHandler
import im.actor.server.api.http.json._
import im.actor.server.dialog.{ GroupDialogRegion, GroupDialogOperations }
import im.actor.server.persist
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

class WebhooksHandler()(
  implicit
  db:                   Database,
  ec:                   ExecutionContext,
  groupProcessorRegion: GroupViewRegion,
  groupDialogRegion:    GroupDialogRegion,
  val materializer:     Materializer
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
          sent ← optGroup match {
            case None                          ⇒ DBIO.successful(Left(StatusCodes.NotFound))
            case Some(group) if group.isPublic ⇒ DBIO.successful(Left(StatusCodes.Forbidden))
            case Some(group) ⇒
              val sendFuture = for {
                isChecked ← GroupOffice.checkAccessHash(group.id, group.accessHash)
                _ ← if (isChecked) {
                  GroupDialogOperations.sendMessage(group.id, bot.userId, 0, ThreadLocalRandom.current().nextLong(), message) map (_ ⇒ ())
                } else {
                  Future.successful(())
                }
              } yield Right(())
              DBIO.from(sendFuture)
          }
        } yield sent
      } getOrElse DBIO.successful(Left(StatusCodes.BadRequest))
    } yield result
    db.run(action)
  }

}
