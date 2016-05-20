package im.actor.server.file

import java.time.Instant

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import im.actor.acl.ACLFiles
import im.actor.server.api.http.HttpHandler
import im.actor.server.api.http.HttpApiHelpers._
import im.actor.server.db.DbExtension
import im.actor.server.model.{ File ⇒ FileModel }
import im.actor.server.persist.files.FileRepo
import im.actor.util.log.AnyRefLogSource
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.HmacUtils
import scodec.bits.BitVector
import scodec._
import scodec.codecs.{ ascii32, byte, int32 }

import scala.util.Success

object FileUrlBuilderRejections {
  case object FileBuilderExpiredRejection extends ActorCustomRejection
  case object FileNotFoundRejection extends ActorCustomRejection
  case object InvalidVersionRejection extends ActorCustomRejection
  case object InvalidBuilderSignatureRejection extends ActorCustomRejection
}

private[file] final class FileUrlBuilderHttpHandler(fsAdapter: FileStorageAdapter)(implicit system: ActorSystem) extends HttpHandler with AnyRefLogSource {
  import FileUrlBuilderRejections._

  private val db = DbExtension(system).db
  private val log = Logging(system, this)

  val rejectionHandler: RejectionHandler =
    RejectionHandler.newBuilder()
      .handle {
        case FileBuilderExpiredRejection ⇒
          complete(StatusCodes.Gone → "FileUrlBuilder expired; request new builder!")
      }
      .handle {
        case FileNotFoundRejection ⇒
          complete(StatusCodes.NotFound → "File not found")
      }
      .handle {
        case InvalidBuilderSignatureRejection ⇒
          complete(StatusCodes.Forbidden → "Invalid file signature in file builder")
      }
      .handle {
        case InvalidVersionRejection ⇒
          complete(StatusCodes.BadRequest → "Wrong version")
      }
      .result()

  // format: OFF
  def routes: Route =
    extractRequest { request =>
      defaultVersion {
        pathPrefix("files" / SignedLongNumber) { fileId =>
          get {
            validateBuilderRequest(fileId) { case (fileModel, accessHash) =>
              log.debug("Got file url builder request: {}", request)
              onSuccess(fsAdapter.getFileDownloadUrl(fileModel, accessHash)) {
                case Some(url) => redirect(url, StatusCodes.Found)
                case None => complete(StatusCodes.NotFound -> "File not found")
              }
            }
          }
        }
      }
    }
  // format: ON

  // monad transformers could make this code easier
  private def validateBuilderRequest(fileId: Long): Directive1[(FileModel, Long)] =
    parameter("signature") flatMap {
      case s ⇒
        s split "_" match {
          case Array(hexSeed, hexSignature) ⇒
            UrlBuilderSeed.validate(Hex.decodeHex(hexSeed.toCharArray)) match {
              case Success(seed) ⇒
                if (seed.version == 0) {
                  onSuccess(db.run(FileRepo.find(fileId))) flatMap {
                    case Some(file) ⇒
                      val seedBytes = seed.toByteArray
                      val expire = seed.expire
                      val secret = ACLFiles.fileUrlBuilderSecret(seedBytes)
                      val accessHash = ACLFiles.fileAccessHash(file.id, file.accessSalt)
                      val valueToDigest = BitVector(seedBytes) ++ BitVector.fromLong(fileId) ++ BitVector.fromLong(accessHash)
                      val calculatedSignature = HmacUtils.hmacSha256Hex(secret, valueToDigest.toByteArray)
                      if (hexSignature == calculatedSignature) {
                        val now = Instant.now
                        if (isExpired(expire, now)) {
                          log.debug(
                            "Signature expired. Signature: {}, expire: {}, now: {}",
                            hexSignature, Instant.ofEpochSecond(expire.toLong), now
                          )
                          reject(FileBuilderExpiredRejection)
                        } else {
                          provide((file, accessHash))
                        }
                      } else {
                        log.debug(
                          "Incorrect signature. Signature from request: {}, calculated signature: {}",
                          hexSignature, calculatedSignature
                        )
                        reject(InvalidBuilderSignatureRejection)
                      }
                    case None ⇒
                      log.debug("File not found, id: {}", fileId)
                      reject(FileNotFoundRejection)
                  }
                } else {
                  log.debug("Wrong version of algo")
                  reject(InvalidVersionRejection)
                }
              case _ ⇒
                log.debug("Unable to decode seed: {}", hexSeed)
                reject(InvalidBuilderSignatureRejection)
            }
          case _ ⇒
            log.debug("Wrong query signature: {}", s)
            reject(InvalidBuilderSignatureRejection)
        }
    }

  private def isExpired(expire: Int, now: Instant): Boolean = Instant.ofEpochSecond(expire.toLong).isBefore(now)

}
