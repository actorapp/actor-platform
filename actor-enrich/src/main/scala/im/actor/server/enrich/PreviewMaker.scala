package im.actor.server.enrich

import scala.concurrent.{ ExecutionContextExecutor, Future }

import akka.actor._
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.{ Http, HttpExt }
import akka.pattern.pipe
import akka.stream.FlowMaterializer
import akka.util.ByteString

import im.actor.server.enrich.RichMessageWorker.MessageInfo

object PreviewMaker {

  def apply(config: RichMessageConfig, name: String)(implicit system: ActorSystem, flowMaterializer: FlowMaterializer): ActorRef =
    system.actorOf(Props(classOf[PreviewMaker], config, flowMaterializer), name)

  object Failures {
    object Messages {
      val NotAnImage = "content is not an image"
      val ContentTooLong = "content is too long"
      val Failed = "failed to make preview"
    }
    def notAnImage(info: MessageInfo) = PreviewFailure(Messages.NotAnImage, info)
    def contentTooLong(info: MessageInfo) = PreviewFailure(Messages.ContentTooLong, info)
    def failedToMakePreview(info: MessageInfo, cause: String = Messages.Failed) = PreviewFailure(cause, info)
    def failedWith(status: StatusCode, info: MessageInfo): PreviewFailure = PreviewFailure(s"failed to make preview with http status code ${status.value}", info)
  }

  import RichMessageWorker.MessageInfo

  case class GetPreview(uri: String, info: MessageInfo)

  sealed trait PreviewResult
  case class PreviewSuccess(content: ByteString, fileName: Option[String], contentType: String, info: MessageInfo) extends PreviewResult
  case class PreviewFailure(message: String, info: MessageInfo) extends PreviewResult

  private def getFileName(cdOption: Option[`Content-Disposition`]) = cdOption.flatMap(_.params.get("filename"))
}

class PreviewMaker(config: RichMessageConfig)(implicit flowMaterializer: FlowMaterializer) extends Actor with ActorLogging {

  import PreviewHelpers._
  import PreviewMaker._

  implicit val system: ActorSystem = context.system
  implicit val ec: ExecutionContextExecutor = context.dispatcher
  implicit val http: HttpExt = Http()

  def receive = {
    case GetPreview(url, info) ⇒
      val result: Future[PreviewResult] = withRequest(HttpRequest(GET, url), info) { response ⇒
        val cd: Option[`Content-Disposition`] = response.header[`Content-Disposition`]
        response match {
          case HttpResponse(_: StatusCodes.Success, _, entity: HttpEntity.Default, _) ⇒ downloadDefault(entity, getFileName(cd), info, config)
          case HttpResponse(_: StatusCodes.Success, _, entity: HttpEntity.Chunked, _) ⇒ downloadChunked(entity, getFileName(cd), info, config)
          case HttpResponse(status, _, _, _) ⇒ Future.successful(Failures.failedWith(status, info))
        }
      }
      result pipeTo sender()
    case _ ⇒
  }
}