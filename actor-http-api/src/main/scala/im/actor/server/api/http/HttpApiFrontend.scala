package im.actor.server.api.http

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes.{ InternalServerError, NotAcceptable, OK }
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.stream.FlowMaterializer
import akka.stream.scaladsl.Sink
import com.github.dwhjames.awswrap.s3.AmazonS3ScalaClient
import play.api.libs.json.Json
import slick.driver.PostgresDriver.api._

import im.actor.server.api.http.JsonImplicits._
import im.actor.server.peermanagers.GroupPeerManagerRegion

object HttpApiFrontend {

  def start(config: HttpApiConfig, s3BucketName: String)(
    implicit
    system:                 ActorSystem,
    materializer:           FlowMaterializer,
    db:                     Database,
    groupPeerManagerRegion: GroupPeerManagerRegion,
    client:                 AmazonS3ScalaClient
  ): Unit = {

    implicit val ec: ExecutionContext = system.dispatcher

    val groupInfo = new GroupInfoHandler(s3BucketName)
    val webhooks = new WebhookHandler()

    //TODO: replace to object, import in scope
    implicit val toContent = Unmarshaller.apply[HttpRequest, Content] { implicit ec ⇒ req ⇒
      req.entity.dataBytes
        .map { data ⇒ Json.parse(data.decodeString("utf-8")).as[Content] }
        .runWith(Sink.head)
    }

    def routes: Route =
      pathPrefix("v1") {
        path("group-invite-info" / Segment) { token ⇒
          (get | post) {
            onComplete(groupInfo.retrieve(token)) {
              case Success(Right(result)) ⇒
                complete(HttpResponse(
                  status = OK,
                  entity = Json.stringify(Json.toJson(result))
                ))
              case Success(Left(errors)) ⇒
                complete(HttpResponse(
                  status = NotAcceptable,
                  entity = Json.stringify(Json.toJson(errors))
                ))
              case Failure(e) ⇒ complete(HttpResponse(InternalServerError))
            }
          }
        } ~
          path("webhooks" / Segment) { token ⇒
            post {
              entity(as[Content]) { content ⇒
                onComplete(webhooks.send(content, token)) {
                  case Success(_) ⇒ complete(HttpResponse(OK))
                  case Failure(e) ⇒ complete(HttpResponse(InternalServerError))
                }
              }
            }
          }
      }

    Http().bind(config.interface, config.port).runForeach { connection ⇒
      connection handleWith Route.handlerFlow(routes)
    }
  }

}
