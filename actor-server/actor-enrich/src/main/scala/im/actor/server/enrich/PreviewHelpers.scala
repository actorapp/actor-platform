package im.actor.server.enrich

import akka.actor.ActorSystem
import akka.http.scaladsl.util.FastFuture
import im.actor.server.enrich.PreviewMaker.Failures._
import im.actor.server.enrich.PreviewMaker._
import spray.client.pipelining._
import spray.http.{ HttpEntity, HttpRequest, HttpResponse }

import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }

trait PreviewHelpers {

  protected implicit val system: ActorSystem

  def withRequest(request: ⇒ HttpRequest, randomId: Long)(f: HttpResponse ⇒ PreviewResult)(implicit system: ActorSystem): Future[PreviewResult] = {
    import system.dispatcher
    val singleRequest: HttpRequest ⇒ Future[HttpResponse] = sendReceive
    Try(request) match {
      case Success(v) ⇒ singleRequest(v) map f recover { case e: Exception ⇒ failedToMakePreview(randomId, e.getMessage) }
      case Failure(_) ⇒ FastFuture.successful(failedToMakePreview(randomId))
    }
  }

  def downloadDefault(entity: HttpEntity.NonEmpty, fileName: Option[String], gp: GetPreview, maxSize: Long): PreviewResult = {
    val mediaType = entity.contentType.mediaType
    val contentLength = entity.data.length
    (mediaType.isImage, contentLength) match {
      case (true, length) if length <= maxSize ⇒
        PreviewSuccess(entity.data.toByteArray, fileName, mediaType.value, gp.clientUserId, gp.peer, gp.randomId)
      case (true, _)  ⇒ contentTooLong(gp.randomId)
      case (false, _) ⇒ notAnImage(gp.randomId)
    }
  }
}
