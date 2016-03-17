package im.actor.server.api.rpc.service.files

import java.time.Instant
import java.time.temporal.ChronoUnit

import akka.actor._
import akka.http.scaladsl.util.FastFuture
import cats.data.Xor
import im.actor.api.rpc.CommonRpcErrors.IntenalError
import im.actor.api.rpc.FileRpcErrors.UnsupportedSignatureAlgorithm
import im.actor.api.rpc._
import im.actor.api.rpc.files._
import im.actor.concurrent.FutureExt
import im.actor.server.acl.ACLUtils
import im.actor.server.api.http.HttpApiConfig
import im.actor.server.db.DbExtension
import im.actor.server.file._
import im.actor.server.persist.files.{ FilePartRepo, FileRepo }
import org.apache.commons.codec.binary.Hex
import slick.driver.PostgresDriver.api._

import scala.concurrent.{ ExecutionContext, Future }

class FilesServiceImpl(implicit actorSystem: ActorSystem) extends FilesService {

  import FutureResultRpc._

  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  private implicit val db: Database = DbExtension(actorSystem).db
  private val fsAdapter: FileStorageAdapter = FileStorageExtension(actorSystem).fsAdapter
  private val httpConfig = HttpApiConfig.load.get

  override def doHandleGetFileUrl(location: ApiFileLocation, clientData: ClientData): Future[HandlerResult[ResponseGetFileUrl]] =
    authorized(clientData) { _ ⇒
      (for {
        file ← fromFutureOption(FileRpcErrors.LocationInvalid)(db.run(FileRepo.find(location.fileId)))
        url ← fromFutureOption(FileRpcErrors.LocationInvalid)(fsAdapter.getFileDownloadUrl(file, location.accessHash))
      } yield ResponseGetFileUrl(url, FileStorageAdapter.UrlExpirationTimeout.toSeconds.toInt, None, Vector.empty)).value
    }

  override def doHandleGetFileUrls(files: IndexedSeq[ApiFileLocation], clientData: ClientData): Future[HandlerResult[ResponseGetFileUrls]] =
    authorized(clientData) { _ ⇒
      val idsHashes = files.map(fl ⇒ fl.fileId → fl.accessHash).toMap
      (for {
        models ← fromFuture(db.run(FileRepo.fetch(idsHashes.keySet)))
        attempts ← fromFuture(FutureExt.ftraverse(models) { model ⇒
          val accessHash = idsHashes.getOrElse(model.id, throw new RuntimeException("Db returned file"))
          (for {
            url ← fromFutureOption(FileRpcErrors.LocationInvalid)(fsAdapter.getFileDownloadUrl(model, accessHash))
          } yield ApiFileUrlDescription(model.id, url, FileStorageAdapter.UrlExpirationTimeout.toSeconds.toInt, None, Vector.empty)).value
        })
        // FIXME: fail-fast here
        urlDescs ← fromXor((e: RpcError) ⇒ e)(attempts.foldLeft(Xor.Right(Nil): Xor[RpcError, List[ApiFileUrlDescription]]) {
          case (Xor.Right(acc), Xor.Right(fd)) ⇒ Xor.Right(fd :: acc)
          case (l: Xor.Left[_], _)             ⇒ l
          case (_, l: Xor.Left[_])             ⇒ l
        })
      } yield ResponseGetFileUrls(urlDescs.toVector)).value
    }

  override def doHandleGetFileUploadUrl(expectedSize: Int, clientData: ClientData): Future[HandlerResult[ResponseGetFileUploadUrl]] =
    authorized(clientData) { _ ⇒
      val id = ACLUtils.randomLong()
      (for {
        uploadKeyUrl ← fromFuture(fsAdapter.getFileUploadUrl(id))
        (uploadKey, url) = uploadKeyUrl
        _ ← fromFuture(db.run(FileRepo.create(id, expectedSize.toLong, accessSalt = ACLUtils.nextAccessSalt(), uploadKey.key)))
      } yield ResponseGetFileUploadUrl(url, uploadKey.toByteArray)).value
    }

  override def doHandleGetFileUploadPartUrl(partNumber: Int, partSize: Int, keyBytes: Array[Byte], clientData: ClientData): Future[HandlerResult[ResponseGetFileUploadPartUrl]] =
    authorized(clientData) { _ ⇒
      (for {
        file ← fromFutureOption(FileRpcErrors.FileNotFound)(db.run(FileRepo.findByKey(fsAdapter.parseKey(keyBytes).key)))
        partKeyUrl ← fromFuture(fsAdapter.getFileUploadPartUrl(file.id, partNumber))
        (partKey, url) = partKeyUrl
        _ ← fromFuture(db.run(FilePartRepo.createOrUpdate(file.id, partNumber, partSize, partKey.key)))
      } yield ResponseGetFileUploadPartUrl(url)).value
    }

  override def doHandleCommitFileUpload(keyBytes: Array[Byte], fileName: String, clientData: ClientData): Future[HandlerResult[ResponseCommitFileUpload]] =
    authorized(clientData) { _ ⇒
      (for {
        file ← fromFutureOption(FileRpcErrors.FileNotFound)(db.run(FileRepo.findByKey(fsAdapter.parseKey(keyBytes).key)))
        partNames ← fromFuture(db.run(FilePartRepo.findByFileId(file.id) map (_.map(_.uploadKey))))
        _ ← fromFuture(fsAdapter.completeFileUpload(file.id, file.size, UnsafeFileName(fileName), partNames))
      } yield ResponseCommitFileUpload(ApiFileLocation(file.id, ACLUtils.fileAccessHash(file.id, file.accessSalt)))).value
    }

  protected def doHandleGetFileUrlBuilder(supportedSignatureAlgorithms: IndexedSeq[String], clientData: ClientData): Future[HandlerResult[ResponseGetFileUrlBuilder]] =
    authorized(clientData) { _ ⇒
      val result = if (supportedSignatureAlgorithms.contains("HMAC_SHA256")) {
        val expire = Instant.now.plus(1, ChronoUnit.HOURS).getEpochSecond.toInt
        val seedBytes = UrlBuilderSeed(version = 0, expire = expire, randomPart = ACLUtils.randomHash()).toByteArray
        Ok(
          ResponseGetFileUrlBuilder(
            baseUrl = s"${httpConfig.baseUri}/v1/files",
            algo = "HMAC_SHA256",
            signatureSecret = ACLUtils.fileUrlBuilderSecret(seedBytes),
            timeout = expire,
            seed = Hex.encodeHexString(seedBytes)
          )
        )
      } else Error(UnsupportedSignatureAlgorithm)
      FastFuture.successful(result)
    }
}
