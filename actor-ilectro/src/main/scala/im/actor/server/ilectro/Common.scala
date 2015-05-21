package im.actor.server.ilectro

import scala.concurrent._
import scala.concurrent.duration._

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.CustomHeader
import akka.stream.ActorFlowMaterializer
import upickle._

import im.actor.server.ilectro.results.Errors

private[ilectro] object Common {

  def processRequest[B](
    request: HttpRequest,
    success: ResponseEntity ⇒ Future[Right[Errors, B]],
    failure: (StatusCode, ResponseEntity) ⇒ Future[Left[Errors, B]]
  )(implicit http: HttpExt, executionContext: ExecutionContext, materializer: ActorFlowMaterializer, authToken: String): Future[Either[Errors, B]] = {
    val modified = request.copy(
      headers = `X-Auth-Token`(authToken) +: request.headers,
      entity = request.entity.withContentType(ContentTypes.`application/json`)
    )
    http.singleRequest(modified).map {
      case HttpResponse(_: StatusCodes.Success, _, entity, _) ⇒ success(entity)
      case HttpResponse(status, _, entity, _)                 ⇒ failure(status, entity)
    }.flatMap(identity).recover {
      case e: upickle.Invalid     ⇒ Left[Errors, B](Errors("JSON error: " + e.getMessage))
      case e: IllegalUriException ⇒ Left[Errors, B](Errors("Wrong uri"))
      case e: TimeoutException    ⇒ Left[Errors, B](Errors("Request timeout"))
    }
  }

  def defaultFailure[B](implicit executionContext: ExecutionContext, materializer: ActorFlowMaterializer): (StatusCode, ResponseEntity) ⇒ Future[Left[Errors, B]] =
    (status, entity) ⇒
      entity.toStrict(5.seconds).map { e ⇒
        Left(read[Errors](e.data.decodeString("utf-8")).copy(status = Some(status.intValue())))
      }

  final case class `X-Auth-Token`(token: String) extends CustomHeader {
    override def value() = token

    override def name() = "X-Auth-Token"
  }

  case class Data[T](data: T)

}
