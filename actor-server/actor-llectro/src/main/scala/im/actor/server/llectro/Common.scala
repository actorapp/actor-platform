package im.actor.server.llectro

import scala.concurrent._
import scala.concurrent.duration._

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.CustomHeader
import akka.stream.ActorFlowMaterializer
import play.api.libs.json.Json

import im.actor.server.llectro.results.Errors

private[llectro] object Common {
  import JsonReads._

  def processRequest[B](
    request:   HttpRequest,
    onSuccess: ResponseEntity ⇒ Future[Right[Errors, B]],
    onFailure: (StatusCode, ResponseEntity) ⇒ Future[Left[Errors, B]]
  )(implicit http: HttpExt, executionContext: ExecutionContext, system: ActorSystem, materializer: ActorFlowMaterializer, authToken: String): Future[Either[Errors, B]] = {
    val modified = request.copy(
      headers = `X-Auth-Token`(authToken) +: request.headers,
      entity = request.entity.withContentType(ContentTypes.`application/json`)
    )

    system.log.debug("Request {}", modified)

    http.singleRequest(modified).map {
      case HttpResponse(_: StatusCodes.Success, _, entity, _) ⇒ onSuccess(entity)
      case HttpResponse(status, _, entity, _)                 ⇒ onFailure(status, entity)
    }.flatMap(identity).recover {
      // TODO: process json validation errors
      //case e: upickle.Invalid     ⇒ Left[Errors, B](Errors("JSON error: " + e.getMessage))
      case e: IllegalUriException ⇒ Left[Errors, B](Errors("Wrong uri"))
      case e: TimeoutException    ⇒ Left[Errors, B](Errors("Request timeout"))
    }
  }

  def defaultFailure[B](implicit executionContext: ExecutionContext, materializer: ActorFlowMaterializer): (StatusCode, ResponseEntity) ⇒ Future[Left[Errors, B]] =
    (status, entity) ⇒
      entity.toStrict(5.seconds).map { e ⇒
        Left(Json.parse(e.data.decodeString("utf-8")).validate[Errors].asOpt.get.copy(status = Some(status.intValue())))
      }

  final case class `X-Auth-Token`(token: String) extends CustomHeader {
    override def value() = token

    override def name() = "X-Auth-Token"
  }

  case class Data[T](data: T)

}
