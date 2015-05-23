package im.actor.server.webhooks

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.stream.FlowMaterializer
import akka.stream.scaladsl.Sink
import com.typesafe.config.Config
import play.api.libs.json.Json
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.messaging.MessagingService

case class WebhooksConfig(interface: String, port: Int)

object WebhooksConfig {
  def fromConfig(config: Config): WebhooksConfig =
    WebhooksConfig(
      config.getString("interface"),
      config.getInt("port")
    )
}

object WebhooksFrontend {

  def start(config: WebhooksConfig, service: MessagingService)(implicit system: ActorSystem, materializer: FlowMaterializer, db: Database): Unit = {

    implicit val ec: ExecutionContext = system.dispatcher

    //TODO: replace to object, import in scope
    implicit val toContent = Unmarshaller.apply[HttpRequest, Content] { implicit ec ⇒ req ⇒

      import JsonImplicits._

      req.entity.dataBytes
        .map { data ⇒ Json.parse(data.decodeString("utf-8")).as[Content] }
        .runWith(Sink.head)
    }

    def routes: Route =
      path("v1" / "webhooks" / Segment) { token ⇒
        post {
          entity(as[Content]) { content ⇒
            onComplete(new WebhookHandler(service).send(content, token)) {
              case Success(_) ⇒ complete(HttpResponse(StatusCodes.OK))
              case Failure(e) ⇒ complete(HttpResponse(StatusCodes.InternalServerError))
            }
          }
        }
      }

    Http().bind(config.interface, config.port).runForeach { connection ⇒
      connection handleWith Route.handlerFlow(routes)
    }
  }

}
