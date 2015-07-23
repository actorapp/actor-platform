package im.actor.server.api.http.webhooks

import im.actor.server.group.{ GroupOffice, GroupOfficeRegion }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.util.{ Failure, Success }

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.util.Timeout
import org.joda.time.DateTime
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.messaging.{ Message, TextMessage }
import im.actor.server.api.http.RoutesHandler
import im.actor.server.api.http.json._
import im.actor.server.persist

class WebhooksHandler()(
  implicit
  db:                     Database,
  ec:                     ExecutionContext,
  groupPeerManagerRegion: GroupOfficeRegion,
  val materializer:       Materializer
) extends RoutesHandler with ContentUnmarshaler {

  implicit val timeout: Timeout = Timeout(5.seconds)

  override def routes: Route = path("webhooks" / Segment) { token ⇒
    post {
      entity(as[Content]) { content ⇒
        onComplete(send(content, token)) {
          case Success(_) ⇒ complete(HttpResponse(OK))
          case Failure(e) ⇒ complete(HttpResponse(InternalServerError))
        }
      }
    }
  }

  def send(content: Content, token: String) = {
    val message: Message = content match {
      case Text(text)    ⇒ TextMessage(text, Vector.empty, None)
      case Document(url) ⇒ throw new NotImplementedError()
      case Image(url)    ⇒ throw new NotImplementedError()
    }

    db.run {
      for {
        optBot ← persist.GroupBot.findByToken(token)
        userAuth ← optBot.map { bot ⇒
          for {
            optGroup ← persist.Group.find(bot.groupId)
            authIds ← persist.AuthId.findByUserId(bot.userId)

            authId ← (optGroup, authIds) match {
              case (None, _) ⇒ DBIO.successful(None)
              case (Some(group), auth +: _) ⇒
                DBIO.from(GroupOffice.sendMessage(group.id, bot.userId, auth.id, group.accessHash, ThreadLocalRandom.current().nextLong(), message))
              case (Some(group), Seq()) ⇒
                val rng = ThreadLocalRandom.current()
                val authId = rng.nextLong()
                for {
                  _ ← persist.AuthId.create(authId, Some(bot.userId), None)
                  _ ← DBIO.from(GroupOffice.sendMessage(group.id, bot.userId, authId, group.accessHash, rng.nextLong(), message))
                } yield ()
            }
          } yield ()
        }.getOrElse(DBIO.successful(None))
      } yield ()
    }
  }

}
