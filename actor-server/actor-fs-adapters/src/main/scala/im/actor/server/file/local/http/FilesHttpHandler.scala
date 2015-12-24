package im.actor.server.file.local.http

import java.time.{ Duration, Instant }

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ Directive0, Route }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import im.actor.server.api.http.HttpHandler
import im.actor.server.db.DbExtension
import im.actor.server.file.local.{ FileStorageOperations, LocalFileStorageConfig, RequestSigning }
import im.actor.util.log.AnyRefLogSource

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

private[local] final class FilesHttpHandler(storageConfig: LocalFileStorageConfig)(implicit val system: ActorSystem)
  extends HttpHandler
  with RequestSigning
  with FileStorageOperations
  with AnyRefLogSource {

  private val db = DbExtension(system).db
  private implicit val mat = ActorMaterializer()

  protected implicit val ec: ExecutionContext = system.dispatcher
  protected val storageLocation = storageConfig.location

  private val log = Logging(system, this)

  // format: OFF
  def routes: Route =
    extractRequest { request =>
      //      log.debug("Got file request {}", request)
      defaultVersion {
        pathPrefix("files" / Segment) { strFileId =>
          val fileId = strFileId.toLong
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
                val optName = seqName.headOption
                log.debug("Download file request, fileId: {}, fileName: {}", fileId, optName)
                withRangeSupport {
                  onComplete(getFile(fileId, optName)) {
                    case Success(file) =>
                      log.debug("Serving file: {} parts", fileId)
                      complete(file.loadBytes)
                    case Failure(e) =>
                      log.error(e, "Failed to get file content, fileId: {}", fileId)
                      complete(HttpResponse(500))
                  }
                }
              }
            } ~
            put {
              pathSuffix(IntNumber) { partNumber =>
                log.debug("Upload file part request, fileId: {}, partNumber: {}", fileId, partNumber)
                extractRequest { req =>
                  val writeFu = for {
                    _ <- prepareForPartWrite(fileId, partNumber)
                    _ <- req.entity.dataBytes
                      .flatMapConcat[Unit](bs => Source(writeContent(bs, fileId, partNumber)))
                      .runForeach(_ => ())
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

  private def writeContent(bs: ByteString, fileId: Long, partNumber: Int): Future[Unit] =
    appendPartBytes(bs.toArray, fileId, partNumber)

  def validateRequest: Directive0 =
    extractRequestContext flatMap { ctx ⇒
      parameters(("signature", "expires".as[Long])) tflatMap {
        case (signature, expiresAt) ⇒
          val request = ctx.request
          val uriWithoutSignature = request.uri.withQuery(request.uri.query() filterNot { case (k, _) ⇒ k == "signature" })
          val notExpired = isNotExpired(expiresAt)
          val calculatedSignature = calculateSignature(request.method, uriWithoutSignature)
          if (notExpired && calculatedSignature == signature) pass else {
            log.debug("Failed to validate request: {}, notExpired: {}, signature: {}; calculated signature: {}", notExpired, ctx.request, signature, calculatedSignature)
            reject
          }
      }
    }

  private def isNotExpired(expiresAt: Long) = expiresAt <= Instant.now.plus(Duration.ofDays(1)).toEpochMilli

}
