package im.actor.server.enrich

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpEntity, HttpRequest, HttpResponse }
import akka.stream.Materializer
import akka.stream.scaladsl._
import akka.stream.stage.{ Context, PushStage }
import akka.util.ByteString
import im.actor.server.enrich.PreviewMaker.Failures._
import im.actor.server.enrich.PreviewMaker._

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

trait PreviewHelpers {

  protected implicit val system: ActorSystem
  protected implicit val ec: ExecutionContext
  protected implicit val mat: Materializer

  def withRequest(request: ⇒ HttpRequest, randomId: Long)(function: HttpResponse ⇒ Future[PreviewResult])(implicit system: ActorSystem): Future[PreviewResult] = {
    Try(request) match {
      case Success(v) ⇒ Http().singleRequest(v).flatMap(function) recover { case e: Exception ⇒ failedToMakePreview(randomId, e.getMessage) }
      case Failure(_) ⇒ Future.successful(failedToMakePreview(randomId))
    }
  }

  def downloadDefault(entity: HttpEntity.Default, fileName: Option[String], gp: GetPreview, maxSize: Long): Future[PreviewResult] = {
    val mediaType = entity.contentType.mediaType
    val contentLength = entity.contentLength
    (mediaType.isImage, contentLength) match {
      case (true, length) if length <= maxSize ⇒
        entity.dataBytes.runFold(ByteString.empty)(_ ++ _)
          .map { body ⇒ PreviewSuccess(body, fileName, mediaType.value, gp.clientUserId, gp.peer, gp.randomId) }
          .recover { case e: Exception ⇒ failedToMakePreview(gp.randomId, e.getMessage) }
      case (true, _)  ⇒ Future.successful(contentTooLong(gp.randomId))
      case (false, _) ⇒ Future.successful(notAnImage(gp.randomId))
    }
  }

  def downloadChunked(entity: HttpEntity.Chunked, fileName: Option[String], gp: GetPreview, maxSize: Long): Future[PreviewResult] = {
    val mediaType = entity.contentType.mediaType
    mediaType.isImage match {
      case true ⇒
        entity.dataBytes
          .via(sizeBoundingFlow(maxSize))
          .runFold(ByteString.empty)(_ ++ _)
          .map { body ⇒ PreviewSuccess(body, fileName, mediaType.value, gp.clientUserId, gp.peer, gp.randomId) }
          .recover { case e: Exception ⇒ failedToMakePreview(gp.randomId, e.getMessage) }
      case false ⇒ Future.successful(notAnImage(gp.randomId))
    }
  }

  private def sizeBoundingFlow(maxSize: Long) = Flow[ByteString].transform {
    () ⇒
      new PushStage[ByteString, ByteString] {
        var length: Int = 0

        def onPush(elem: ByteString, ctx: Context[ByteString]) =
          if (length > maxSize) {
            ctx.fail(new Exception(Failures.Messages.ContentTooLong))
          } else {
            length += elem.length
            ctx.push(elem)
          }
      }
  }

}
