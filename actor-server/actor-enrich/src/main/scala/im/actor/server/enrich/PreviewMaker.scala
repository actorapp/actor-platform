package im.actor.server.enrich

import scala.concurrent.{ ExecutionContextExecutor, Future }

import akka.actor._
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.{ Http, HttpExt }
import akka.pattern.pipe
import akka.stream.Materializer
import akka.util.ByteString

object PreviewMaker {

  def apply(config: RichMessageConfig, name: String)(implicit system: ActorSystem, materializer: Materializer): ActorRef =
    system.actorOf(Props(classOf[PreviewMaker], config, materializer), name)

  object Failures {
    object Messages {
      val NotAnImage = "content is not an image"
      val ContentTooLong = "content is too long"
      val Failed = "failed to make preview"
    }
    def notAnImage(handler: UpdateHandler) = PreviewFailure(Messages.NotAnImage, handler)
    def contentTooLong(handler: UpdateHandler) = PreviewFailure(Messages.ContentTooLong, handler)
    def failedToMakePreview(handler: UpdateHandler, cause: String = Messages.Failed) = PreviewFailure(cause, handler)
    def failedWith(status: StatusCode, handler: UpdateHandler): PreviewFailure = PreviewFailure(s"failed to make preview with http status code ${status.value}", handler)
  }

  case class GetPreview(url: String, handler: UpdateHandler)

  sealed trait PreviewResult
  case class PreviewSuccess(content: ByteString, fileName: Option[String], contentType: String, handler: UpdateHandler) extends PreviewResult
  case class PreviewFailure(message: String, handler: UpdateHandler) extends PreviewResult

  private def getFileName(cdOption: Option[`Content-Disposition`]) = cdOption.flatMap(_.params.get("filename"))
}

class PreviewMaker(config: RichMessageConfig)(implicit materializer: Materializer) extends Actor with ActorLogging {

  import PreviewHelpers._
  import PreviewMaker._

  implicit val system: ActorSystem = context.system
  implicit val ec: ExecutionContextExecutor = context.dispatcher
  implicit val http: HttpExt = Http()

  def receive = {
    case GetPreview(url, handler) ⇒
      val result: Future[PreviewResult] = withRequest(HttpRequest(GET, url), handler) { response ⇒
        val cd: Option[`Content-Disposition`] = response.header[`Content-Disposition`]
        response match {
          case HttpResponse(_: StatusCodes.Success, _, entity: HttpEntity.Default, _) ⇒ downloadDefault(entity, getFileName(cd), handler, config)
          case HttpResponse(_: StatusCodes.Success, _, entity: HttpEntity.Chunked, _) ⇒ downloadChunked(entity, getFileName(cd), handler, config)
          case HttpResponse(status, _, _, _) ⇒ Future.successful(Failures.failedWith(status, handler))
        }
      }
      result pipeTo sender()
    case _ ⇒
  }
}