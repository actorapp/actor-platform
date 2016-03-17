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
import org.apache.commons.codec.digest.HmacUtils
import scodec.bits.BitVector
import scodec._
import scodec.codecs.{ ascii32, byte, int32 }

object FileUrlBuilderRejections {
  case object FileBuilderExpiredRejection extends ActorCustomRejection
  case object FileNotFoundRejection extends ActorCustomRejection
  case object InvalidBuilderSignatureRejection extends ActorCustomRejection
}

final case class Seed(version: Byte, expire: Int, randomPart: String)
object SeedCodec {
  implicit val seedCodec: Codec[Seed] = (byte :: int32 :: ascii32).as[Seed]
}

private[file] final class FileUrlBuilderHttpHandler(fsAdapter: FileStorageAdapter)(implicit system: ActorSystem) extends HttpHandler with AnyRefLogSource {
  import FileUrlBuilderRejections._
  import SeedCodec._

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
      .result()

  // format: OFF
  def routes: Route =
    extractRequest { request =>
      log.debug("Got file url builder request: {}", request)
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
  // format: ON

  // monad transformers could make this code easier
  private def validateBuilderRequest(fileId: Long): Directive1[(FileModel, Long)] =
    parameter("signature") flatMap {
      case s ⇒
        s split "_" match {
          case Array(hexSeed, hexSignature) ⇒
            (for {
              bitSeed ← BitVector.fromHex(hexSeed)
              seed ← Codec.decode[Seed](bitSeed).toOption
            } yield bitSeed → seed) match {
              case Some((bitSeed, DecodeResult(seed, BitVector.empty))) ⇒
                onSuccess(db.run(FileRepo.find(fileId))) flatMap {
                  case Some(file) ⇒
                    val expire = seed.expire
                    val secret = ACLFiles.fileUrlBuilderSecret(bitSeed.toByteArray)
                    val accessHash = ACLFiles.fileAccessHash(file.id, file.accessSalt)
                    val valueToDigest = bitSeed ++ BitVector.fromLong(fileId) ++ BitVector.fromLong(accessHash)
                    val calculatedSignature = HmacUtils.hmacSha256Hex(secret, valueToDigest.toByteArray)
                    if (hexSignature == calculatedSignature) {
                      val now = Instant.now
                      if (isExpired(expire, now)) {
                        log.debug(
                          "Signature expired. Signature: {}, expire: {}, now: {}",
                          hexSignature, expire, Instant.now
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
              case _ ⇒
                log.debug("Unable to decode seed: {}", hexSeed)
                reject(InvalidBuilderSignatureRejection)
            }
          case _ ⇒
            log.debug("Wrong query signature: {}", s)
            reject(InvalidBuilderSignatureRejection)
        }
    }

  private def isExpired(expire: Int, now: Instant): Boolean = Instant.ofEpochSecond(expire.toLong).isAfter(now)

}
