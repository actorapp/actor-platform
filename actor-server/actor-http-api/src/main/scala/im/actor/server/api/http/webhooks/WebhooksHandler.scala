package im.actor.server.api.http.webhooks

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ StatusCode, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.util.Timeout
import im.actor.api.rpc.messaging.{ Message, TextMessage }
import im.actor.server.api.http.RoutesHandler
import im.actor.server.api.http.json._
import im.actor.server.commons.KeyValueMappings
import im.actor.server.dialog.group.{ GroupDialogOperations, GroupDialogRegion }
import im.actor.server.group.{ GroupOffice, GroupViewRegion }
import shardakka.{ IntCodec, ShardakkaExtension }

import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

class WebhooksHandler()(
  implicit
  system:               ActorSystem,
  ec:                   ExecutionContext,
  groupProcessorRegion: GroupViewRegion,
  groupDialogRegion:    GroupDialogRegion,
  val materializer:     Materializer
) extends RoutesHandler with ContentUnmarshaler {

  implicit val timeout: Timeout = Timeout(5.seconds)
  private val kv = ShardakkaExtension(system).simpleKeyValue[Int](KeyValueMappings.IntegrationTokens, IntCodec)

  override def routes: Route = path("webhooks" / Segment) { token ⇒
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
      optGroupId ← kv.get(token)
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
