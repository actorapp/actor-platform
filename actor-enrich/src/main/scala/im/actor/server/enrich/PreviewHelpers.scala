package im.actor.server.enrich

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{ HttpEntity, HttpRequest, HttpResponse }
import akka.stream.FlowMaterializer
import akka.stream.scaladsl._
import akka.stream.stage.{ Context, PushStage }
import akka.util.ByteString

import im.actor.server.enrich.PreviewMaker.Failures._
import im.actor.server.enrich.PreviewMaker._
import im.actor.server.enrich.RichMessageWorker.MessageInfo

object PreviewHelpers {

  def withRequest(request: ⇒ HttpRequest)(function: HttpResponse ⇒ Future[PreviewResult])(implicit http: HttpExt, flowMaterializer: FlowMaterializer, ec: ExecutionContext): Future[PreviewResult] = {
    Try(request) match {
      case Success(v) ⇒ http.singleRequest(v).flatMap(function).recover { case e: Exception ⇒ failedToMakePreview(e.getMessage) }
      case Failure(_) ⇒ Future.successful(failedToMakePreview())
    }
  }

  def downloadDefault(entity: HttpEntity.Default, fileName: Option[String], info: MessageInfo, config: RichMessageConfig)(implicit flowMaterializer: FlowMaterializer, ec: ExecutionContext): Future[PreviewResult] = {
    val mediaType = entity.contentType.mediaType
    val contentLength = entity.contentLength
    (mediaType.isImage, contentLength) match {
      case (true, length) if length <= config.maxSize ⇒
        entity
          .toStrict(10.seconds)
          .map { body ⇒ PreviewSuccess(body.data, fileName, mediaType.value, info) }
          .recover { case e: Exception ⇒ failedToMakePreview(e.getMessage) }
      case (true, _)  ⇒ Future.successful(ContentTooLong)
      case (false, _) ⇒ Future.successful(NotAnImage)
    }
  }

  def downloadChunked(entity: HttpEntity.Chunked, fileName: Option[String], info: MessageInfo, config: RichMessageConfig)(implicit flowMaterializer: FlowMaterializer, ec: ExecutionContext): Future[PreviewResult] = {
    val mediaType = entity.contentType.mediaType
    mediaType.isImage match {
      case true ⇒
        entity.dataBytes
          .via(sizeBoundingFlow(config.maxSize))
          .runWith(Sink.fold(ByteString.empty) { (acc, el) ⇒ acc ++ el })
          .map { body ⇒ PreviewSuccess(body, fileName, mediaType.value, info) }
          .recover { case e: Exception ⇒ failedToMakePreview(e.getMessage) }
      case false ⇒ Future.successful(NotAnImage)
    }
  }

  private def sizeBoundingFlow(maxSize: Long) = Flow[ByteString].transform {
    () ⇒
      new PushStage[ByteString, ByteString] {
        var length: Int = 0

        def onPush(elem: ByteString, ctx: Context[ByteString]) =
          if (length > maxSize) {
            ctx.fail(new Exception(ContentTooLong.message))
          } else {
            length += elem.length
            ctx.push(elem)
          }
      }
  }

}
