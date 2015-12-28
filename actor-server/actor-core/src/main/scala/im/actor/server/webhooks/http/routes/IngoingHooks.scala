package im.actor.server.webhooks.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ StatusCode, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.Segment
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import im.actor.api.rpc.messaging.{ ApiMessage, ApiTextMessage }
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.server.api.http.json._
import im.actor.server.dialog.DialogExtension
import im.actor.server.group.GroupExtension

import scala.concurrent.Future
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.util.{ Failure, Success }

trait IngoingHooks extends ContentUnmarshaller with PlayJsonSupport {
  self: WebhooksHttpHandler ⇒

  import JsonFormatters._

  def ingoing: Route = path(Segment) { token ⇒
    post {
      entity(as[Content]) { content ⇒
        onComplete(send(content, token)) {
          case Success(result) ⇒
            result match {
              case Left(statusCode) ⇒ complete(statusCode → Status("failure"))
              case Right(_)         ⇒ complete(OK → Status("Ok"))
            }
          case Failure(e) ⇒
            system.log.error(e, "Failed to handle ingoing hook")
            complete(InternalServerError)
        }
      }
    }
  }

  def send(content: Content, token: String): Future[Either[StatusCode, Unit]] = {
    val message: ApiMessage = content match {
      case Text(text)    ⇒ ApiTextMessage(text, Vector.empty, None)
      case Document(url) ⇒ throw new Exception("Not implementer")
      case Image(url)    ⇒ throw new Exception("Not implementer")
    }

    for {
      optGroupId ← integrationTokensKv.get(token)
      result ← optGroupId map { groupId ⇒
        for {
          (_, _, optBot) ← GroupExtension(system).getMemberIds(groupId)
          _ ← optBot map { botId ⇒
            DialogExtension(system).sendMessage(ApiPeer(ApiPeerType.Group, groupId), botId, 0, ThreadLocalRandom.current().nextLong(), message)
          } getOrElse Future.successful(Left(StatusCodes.NotAcceptable))
        } yield Right(())
      } getOrElse Future.successful(Left(StatusCodes.BadRequest))
    } yield result
  }

}
