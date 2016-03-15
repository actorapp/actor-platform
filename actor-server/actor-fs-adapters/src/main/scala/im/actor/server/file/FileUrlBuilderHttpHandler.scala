package im.actor.server.file

import java.time.Instant

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import im.actor.acl.ACLFiles
import im.actor.server.api.http.HttpHandler
import im.actor.server.db.DbExtension
import im.actor.server.model.{ File ⇒ FileModel }
import im.actor.server.persist.files.FileRepo
import im.actor.util.log.AnyRefLogSource
import org.apache.commons.codec.digest.HmacUtils
import scodec.bits.BitVector

object FileUrlBuilderRejections {
  case object SecretExpiredRejection extends Rejection
  case object FileNotFoundRejection extends Rejection
  case object IncorrectSignatureRejection extends Rejection
}

private[file] final class FileUrlBuilderHttpHandler(fsAdapter: FileStorageAdapter)(implicit system: ActorSystem) extends HttpHandler with AnyRefLogSource {
  import FileUrlBuilderRejections._

  private val db = DbExtension(system).db
  private val log = Logging(system, this)

  private val myRejectionHandler: RejectionHandler =
    RejectionHandler.newBuilder()
      .handle {
        case SecretExpiredRejection ⇒
          complete(StatusCodes.Gone → "FileUrlBuilder expired; request new builder!")
      }
      .handle {
        case FileNotFoundRejection ⇒
          complete(StatusCodes.NotFound → "File not found")
      }
      .handle {
        case IncorrectSignatureRejection ⇒
          complete(StatusCodes.Forbidden → "Incorrect file signature")
      }
      .result()

  // format: OFF
  def routes: Route =
    handleRejections(myRejectionHandler) {
      defaultVersion {
        pathPrefix("fileUrlBuilder") {
          parameter("fileId".as[Long]) { fileId =>
            get {
              validateBuilderRequest(fileId) { case (fileModel, accessHash) =>
                onSuccess(fsAdapter.getFileDownloadUrl(fileModel, accessHash)) {
                  case Some(url) => redirect(url, StatusCodes.Found)
                  case None => complete(StatusCodes.NotFound -> "File not found")
                }
              }
            }
          }
        }
      }
    }
  // format: ON

  def validateBuilderRequest(fileId: Long): Directive1[(FileModel, Long)] =
    parameters(("seed", "expire".as[Long], "signature", "accessHash".as[Long])) tflatMap {
      case (seed, expire, signature, accessHash) ⇒
        val secret = BitVector.fromLong(ACLFiles.fileUrlBuilderSecret(seed, expire)).toByteArray
        if (signature == HmacUtils.hmacSha256Hex(secret, s"$fileId$accessHash".getBytes)) {
          if (isExpired(expire)) {
            reject(SecretExpiredRejection)
          } else {
            onSuccess(db.run(FileRepo.find(fileId))) flatMap {
              case Some(file) ⇒ provide((file, accessHash))
              case None       ⇒ reject(FileNotFoundRejection)
            }
          }
        } else {
          reject(IncorrectSignatureRejection)
        }
    }

  def isExpired(expire: Long): Boolean = Instant.ofEpochSecond(expire).isAfter(Instant.now)

}
