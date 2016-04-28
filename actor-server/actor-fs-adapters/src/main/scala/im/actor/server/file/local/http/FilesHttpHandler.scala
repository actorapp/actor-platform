package im.actor.server.file.local.http

import java.time.{ Duration, Instant }

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model.headers.ContentDispositionTypes.attachment
import akka.http.scaladsl.model.headers.`Content-Disposition`
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import im.actor.server.api.http.HttpHandler
import im.actor.server.api.http.HttpApiHelpers._
import im.actor.server.file.local.http.fix.GetFileFix
import im.actor.server.file.local.{ FileStorageOperations, LocalFileStorageConfig, RequestSigning }
import im.actor.util.log.AnyRefLogSource

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

private object FilesRejections {
  case object InvalidFileSignature extends ActorCustomRejection
}

private[local] final class FilesHttpHandler(storageConfig: LocalFileStorageConfig)(implicit val system: ActorSystem)
  extends HttpHandler
  with RequestSigning
  with FileStorageOperations
  with AnyRefLogSource
  with GetFileFix {
  import FilesRejections._

  protected implicit val mat = ActorMaterializer()

  protected implicit val ec: ExecutionContext = system.dispatcher
  protected val storageLocation = storageConfig.location

  private val log = Logging(system, this)

  val rejectionHandler: RejectionHandler =
    RejectionHandler.newBuilder()
      .handle {
        case InvalidFileSignature ⇒
          complete(StatusCodes.Forbidden → "Invalid file signature")
      }
      .result()

  // format: OFF
  def routes: Route =
    extractRequest { request =>
      //      log.debug("Got file request {}", request)
      defaultVersion {
        pathPrefix("files" / SignedLongNumber) { fileId =>
          options {
            log.debug("Responded OK to OPTIONS req: {}", request.uri)
            complete(HttpResponse(OK))
          } ~
          validateRequest {
            get {
              //we use `Segments` because have to match paths like:
              //v1/files/:fileId/:fileName
              //v1/files/:fileId
              path(Segments(0, 1)) { seqName =>
                log.debug("Download file request, fileId: {}", fileId)
                onComplete(getFile(fileId)) {
                  case Success(Some(file)) =>
                    log.debug("Serving fileId: {}, file: {} parts", fileId, file)
                    respondWithDefaultHeader(
                      `Content-Disposition`(attachment, Map("filename" -> file.name))
                    ) {
                      //TODO: remove as soon, as https://github.com/akka/akka/issues/20338 get fixed
                      getFromFileFix(file.toJava)
                    }
                  case Success(None) =>
                    complete(HttpResponse(StatusCodes.NotFound))
                  case Failure(e) =>
                    log.error(e, "Failed to get file content, fileId: {}", fileId)
                    complete(HttpResponse(500))
                }
              }
            } ~
            put {
              pathSuffix(IntNumber) { partNumber =>
                log.debug("Upload file part request, fileId: {}, partNumber: {}", fileId, partNumber)
                extractRequest { req =>
                  val writeFu = for {
                    _ <- prepareForPartWrite(fileId, partNumber)
                    _ <- appendPartBytes(req.entity.dataBytes, fileId, partNumber)
                    _ <- Future {}
                  } yield ()
                  onComplete(writeFu) {
                    case Success(_) =>
                      log.debug("Successfully uploaded part #{} of file: {} ", partNumber, fileId)
                      complete(HttpResponse(200))
                    case Failure(e) =>
                      log.error(e, "Failed to upload file: {}", fileId)
                      complete(HttpResponse(500))
                  }
                }
              }
            }
          }
        }
      }
    }
  // format: ON

  def validateRequest: Directive0 =
    extractRequestContext.flatMap[Unit] { ctx ⇒
      parameters(("signature", "expires".as[Long])) tflatMap {
        case (signature, expiresAt) ⇒
          val request = ctx.request
          val uriWithoutSignature = request.uri.withQuery(request.uri.query() filterNot { case (k, _) ⇒ k == "signature" })
          val notExpired = isNotExpired(expiresAt)
          val calculatedSignature = calculateSignature(request.method, uriWithoutSignature)
          if (notExpired && calculatedSignature == signature) pass else {
            log.debug("Failed to validate request: {}, notExpired: {}, signature: {}; calculated signature: {}", notExpired, ctx.request, signature, calculatedSignature)
            reject(InvalidFileSignature)
          }
      }
    }

  private def isNotExpired(expiresAt: Long) = expiresAt <= Instant.now.plus(Duration.ofDays(1)).toEpochMilli

}
