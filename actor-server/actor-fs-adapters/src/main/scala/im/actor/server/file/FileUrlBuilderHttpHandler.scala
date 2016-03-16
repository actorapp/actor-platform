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
import scodec._
import scodec.codecs.{ byte, int32, long }

object FileUrlBuilderRejections {
  case object SecretExpiredRejection extends Rejection
  case object FileNotFoundRejection extends Rejection
  case object IncorrectSignatureRejection extends Rejection
}

final case class Seed(version: Byte, expire: Int, randomPart: Long)
object SeedDecoder {
  implicit val seedDecoder = (byte :: int32 :: long(64)).as[Seed]
}

private[file] final class FileUrlBuilderHttpHandler(fsAdapter: FileStorageAdapter)(implicit system: ActorSystem) extends HttpHandler with AnyRefLogSource {
  import FileUrlBuilderRejections._
  import SeedDecoder._

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
    extractRequest { request =>
      log.debug("Got file url builder request: {}", request)
      handleRejections(myRejectionHandler) {
        defaultVersion {
          pathPrefix("files" / SignedLongNumber) { fileId =>
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

  // monad transformers could make this code easier
  def validateBuilderRequest(fileId: Long): Directive1[(FileModel, Long)] =
    parameter("signature") flatMap {
      case s ⇒
        s split "_" match {
          case Array(originalSeed, originalSignature) ⇒
            (for {
              bitSeed ← BitVector.fromHex(originalSeed)
              seed ← Codec.decode[Seed](bitSeed).toOption
            } yield seed) match {
              case Some(DecodeResult(seed, BitVector.empty)) ⇒
                onSuccess(db.run(FileRepo.find(fileId))) flatMap {
                  case Some(file) ⇒
                    val expire = seed.expire
                    val secret = BitVector.fromLong(ACLFiles.fileUrlBuilderSecret(originalSeed, expire)).toHex
                    val accessHash = ACLFiles.fileAccessHash(file.id, file.accessSalt)
                    val calculatedSignature = HmacUtils.hmacSha256Hex(secret, s"$fileId$accessHash")
                    if (originalSignature == calculatedSignature) {
                      val now = Instant.now
                      if (isExpired(expire, now)) {
                        log.debug(
                          "Signature expired. Signature: {}, expire: {}, now: {}",
                          originalSignature, expire, Instant.now
                        )
                        reject(SecretExpiredRejection)
                      } else {
                        provide((file, accessHash))
                      }
                    } else {
                      log.debug(
                        "Incorrect signature. Signature from request: {}, calculated signature: {}",
                        originalSignature, calculatedSignature
                      )
                      reject(IncorrectSignatureRejection)
                    }
                  case None ⇒
                    log.debug("File not found, id: {}", fileId)
                    reject(FileNotFoundRejection)
                }
              case _ ⇒
                log.debug("Unable to decode seed: {}", originalSeed)
                reject(IncorrectSignatureRejection)
            }
          case _ ⇒
            log.debug("Wrong query signature: {}", s)
            reject(IncorrectSignatureRejection)
        }
    }

  def isExpired(expire: Int, now: Instant): Boolean = Instant.ofEpochSecond(expire.toLong).isAfter(now)

}
