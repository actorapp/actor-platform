package im.actor.server.api.rpc.service.files

import akka.actor._
import cats.data.Xor
import im.actor.api.rpc.FileHelpers.Errors
import im.actor.api.rpc.files._
import im.actor.api.rpc._
import im.actor.concurrent.FutureExt
import im.actor.server.acl.ACLUtils
import im.actor.server.db.DbExtension
import im.actor.server.file._
import im.actor.server.persist.{ FilePartRepo, FileRepo }
import slick.driver.PostgresDriver.api._

import scala.concurrent.{ ExecutionContext, Future }

class FilesServiceImpl(implicit actorSystem: ActorSystem) extends FilesService {

  import FutureResultRpcCats._

  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  private implicit val db: Database = DbExtension(actorSystem).db
  private val fsAdapter: FileStorageAdapter = FileStorageExtension(actorSystem).fsAdapter

  override def jhandleGetFileUrl(location: ApiFileLocation, clientData: ClientData): Future[HandlerResult[ResponseGetFileUrl]] =
    authorized(clientData) { client ⇒
      (for {
        file ← fromFutureOption(Errors.LocationInvalid)(db.run(FileRepo.find(location.fileId)))
        url ← fromFutureOption(Errors.LocationInvalid)(fsAdapter.getFileDownloadUrl(file, location.accessHash))
      } yield ResponseGetFileUrl(url, FileStorageAdapter.UrlExpirationTimeout.toSeconds.toInt, None, Vector.empty)).value map (_.toScalaz)
    }

  override def jhandleGetFileUrls(files: IndexedSeq[ApiFileLocation], clientData: ClientData): Future[HandlerResult[ResponseGetFileUrls]] =
    authorized(clientData) { client ⇒
      val idsHashes = files.map(fl ⇒ fl.fileId → fl.accessHash).toMap
      (for {
        models ← fromFuture(db.run(FileRepo.fetch(idsHashes.keySet)))
        attempts ← fromFuture(FutureExt.ftraverse(models) { model ⇒
          val accessHash = idsHashes.getOrElse(model.id, throw new RuntimeException("Db returned file"))
          (for {
            url ← fromFutureOption(Errors.LocationInvalid)(fsAdapter.getFileDownloadUrl(model, accessHash))
          } yield ApiFileUrlDescription(model.id, url, FileStorageAdapter.UrlExpirationTimeout.toSeconds.toInt, None, Vector.empty)).value
        })
        // FIXME: fail-fast here
        urlDescs ← fromEither((e: RpcError) ⇒ e)(attempts.foldLeft(Xor.Right(Nil): Xor[RpcError, List[ApiFileUrlDescription]]) {
          case (Xor.Right(acc), Xor.Right(fd)) ⇒ Xor.Right(fd :: acc)
          case (l: Xor.Left[_], _)             ⇒ l
          case (_, l: Xor.Left[_])             ⇒ l
        })
      } yield ResponseGetFileUrls(urlDescs.toVector)).value map (_.toScalaz)
    }

  override def jhandleGetFileUploadUrl(expectedSize: Int, clientData: ClientData): Future[HandlerResult[ResponseGetFileUploadUrl]] =
    authorized(clientData) { client ⇒
      val id = ACLUtils.randomLong()
      (for {
        uploadKeyUrl ← fromFuture(fsAdapter.getFileUploadUrl(id))
        (uploadKey, url) = uploadKeyUrl
        _ ← fromFuture(db.run(FileRepo.create(id, expectedSize.toLong, accessSalt = ACLUtils.nextAccessSalt(), uploadKey.key)))
      } yield ResponseGetFileUploadUrl(url, uploadKey.toByteArray)).value map (_.toScalaz)
    }

  override def jhandleGetFileUploadPartUrl(partNumber: Int, partSize: Int, keyBytes: Array[Byte], clientData: ClientData): Future[HandlerResult[ResponseGetFileUploadPartUrl]] =
    authorized(clientData) { client ⇒
      (for {
        file ← fromFutureOption(Errors.FileNotFound)(db.run(FileRepo.findByKey(fsAdapter.parseKey(keyBytes).key)))
        partKeyUrl ← fromFuture(fsAdapter.getFileUploadPartUrl(file.id, partNumber))
        (partKey, url) = partKeyUrl
        _ ← fromFuture(db.run(FilePartRepo.createOrUpdate(file.id, partNumber, partSize, partKey.key)))
      } yield ResponseGetFileUploadPartUrl(url)).value map (_.toScalaz)
    }

  override def jhandleCommitFileUpload(keyBytes: Array[Byte], fileName: String, clientData: ClientData): Future[HandlerResult[ResponseCommitFileUpload]] =
    authorized(clientData) { client ⇒
      (for {
        file ← fromFutureOption(Errors.FileNotFound)(db.run(FileRepo.findByKey(fsAdapter.parseKey(keyBytes).key)))
        partNames ← fromFuture(db.run(FilePartRepo.findByFileId(file.id) map (_.map(_.uploadKey))))
        _ ← fromFuture(fsAdapter.completeFileUpload(file.id, file.size, UnsafeFileName(fileName), partNames))
      } yield ResponseCommitFileUpload(ApiFileLocation(file.id, ACLUtils.fileAccessHash(file.id, file.accessSalt)))).value map (_.toScalaz)
    }
}
