package im.actor.server.enrich

import akka.actor._
import akka.pattern.pipe
import im.actor.server.model.Peer
import spray.http.HttpHeaders.`Content-Disposition`
import spray.http.HttpMethods.GET
import spray.http._

import scala.concurrent.{ ExecutionContext, Future }

object PreviewMaker {

  def apply(config: RichMessageConfig, name: String)(implicit system: ActorSystem): ActorRef =
    system.actorOf(Props(classOf[PreviewMaker], config), name)

  object Failures {
    object Messages {
      val NotAnImage = "content is not an image"
      val ContentTooLong = "content is too long"
      val Failed = "failed to make preview"
    }
    def notAnImage(randomId: Long) = PreviewFailure(Messages.NotAnImage, randomId)
    def contentTooLong(randomId: Long) = PreviewFailure(Messages.ContentTooLong, randomId)
    def failedToMakePreview(randomId: Long, cause: String = Messages.Failed) = PreviewFailure(cause, randomId)
    def failedWith(status: StatusCode, randomId: Long): PreviewFailure = PreviewFailure(s"failed to make preview with http status code ${status.value}", randomId)
  }

  final case class GetPreview(
    url:          String,
    clientUserId: Int,
    peer:         Peer,
    randomId:     Long
  )

  sealed trait PreviewResult
  final case class PreviewSuccess(
    content:      Array[Byte],
    fileName:     Option[String],
    contentType:  String,
    clientUserId: Int,
    peer:         Peer,
    randomId:     Long
  ) extends PreviewResult
  final case class PreviewFailure(message: String, randomId: Long) extends PreviewResult

  private def getFileName(cdOption: Option[`Content-Disposition`]) = cdOption.flatMap(_.parameters.get("filename"))
}

class PreviewMaker(config: RichMessageConfig) extends Actor with ActorLogging with PreviewHelpers {

  import PreviewMaker._

  protected implicit val system: ActorSystem = context.system
  import system.dispatcher

  def receive = {
    case gp: GetPreview ⇒
      val result: Future[PreviewResult] = withRequest(HttpRequest(GET, gp.url), gp.randomId) { response ⇒
        val cd: Option[`Content-Disposition`] = response.header[`Content-Disposition`]
        response match {
          case HttpResponse(_: StatusCodes.Success, entity: HttpEntity.NonEmpty, _, _) ⇒ downloadDefault(entity, getFileName(cd), gp, config.maxSize)
          case HttpResponse(status, _, _, _) ⇒ Failures.failedWith(status, gp.randomId)
        }
      }
      result pipeTo sender()
    case _ ⇒
  }
}