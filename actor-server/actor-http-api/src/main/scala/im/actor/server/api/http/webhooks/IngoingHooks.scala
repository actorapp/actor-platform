package im.actor.server.api.http.webhooks

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ StatusCode, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.Segment
import akka.http.scaladsl.server.Route
import im.actor.api.rpc.messaging.{ Message, TextMessage }
import im.actor.server.api.http.json._
import im.actor.server.dialog.group.GroupDialogOperations
import im.actor.server.group.GroupOffice

import scala.concurrent.Future
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.util.{ Failure, Success }

trait IngoingHooks extends ContentUnmarshaller {
  self: WebhooksHandler ⇒

  def ingoing: Route = path(Segment) { token ⇒
    post {
      entity(as[Content]) { content ⇒
        onComplete(send(content, token)) {
          case Success(result) ⇒
            result match {
              case Left(statusCode) ⇒ complete(statusCode)
              case Right(_)         ⇒ complete(OK)
            }
          case Failure(e) ⇒ complete(InternalServerError)
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

    for {
      optGroupId ← integrationTokensKv.get(token)
      result ← optGroupId map { groupId ⇒
        for {
          isPublic ← GroupOffice.isPublic(groupId)
          result ← if (isPublic) {
            Future.successful(Left(StatusCodes.Forbidden))
          } else {
            for {
              (_, _, botId) ← GroupOffice.getMemberIds(groupId)
              _ ← GroupDialogOperations.sendMessage(groupId, botId, 0, ThreadLocalRandom.current().nextLong(), message)
            } yield Right(())
          }
        } yield result
      } getOrElse Future.successful(Left(StatusCodes.BadRequest))
    } yield result
  }

}
