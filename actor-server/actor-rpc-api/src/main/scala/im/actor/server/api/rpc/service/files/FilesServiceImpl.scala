package im.actor.server.api.rpc.service.files

import akka.actor._
import im.actor.api.rpc.FileHelpers.Errors
import im.actor.api.rpc.files._
import im.actor.api.rpc.{ ClientData, _ }
import im.actor.server.acl.ACLUtils
import im.actor.server.db.DbExtension
import im.actor.server.file._
import im.actor.server.persist
import slick.driver.PostgresDriver.api._

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.languageFeature.implicitConversions

class FilesServiceImpl(implicit actorSystem: ActorSystem) extends FilesService {

  import FutureResultRpcCats._

  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  private implicit val db: Database = DbExtension(actorSystem).db
  private val fsAdapter: FileStorageAdapter = FileStorageExtension(actorSystem).fsAdapter

  override def jhandleGetFileUrl(location: ApiFileLocation, clientData: ClientData): Future[HandlerResult[ResponseGetFileUrl]] =
    authorized(clientData) { client ⇒
      val timeout = 1.day
      (for {
        file ← fromFutureOption(Errors.LocationInvalid)(db.run(persist.FileRepo.find(location.fileId)))
        url ← fromFutureOption(Errors.LocationInvalid)(fsAdapter.getFileDownloadUrl(file, location.accessHash))
      } yield ResponseGetFileUrl(url, timeout.toSeconds.toInt)).value map (_.toScalaz)
    }

  override def jhandleGetFileUploadUrl(expectedSize: Int, clientData: ClientData): Future[HandlerResult[ResponseGetFileUploadUrl]] =
    authorized(clientData) { client ⇒
      val id = ACLUtils.randomLong()
      (for {
        uploadKeyUrl ← fromFuture(fsAdapter.getFileUploadUrl(id))
        (uploadKey, url) = uploadKeyUrl
        _ ← fromFuture(db.run(persist.FileRepo.create(id, expectedSize.toLong, accessSalt = ACLUtils.nextAccessSalt(), uploadKey.key)))
      } yield ResponseGetFileUploadUrl(url, uploadKey.toByteArray)).value map (_.toScalaz)
    }

  override def jhandleGetFileUploadPartUrl(partNumber: Int, partSize: Int, keyBytes: Array[Byte], clientData: ClientData): Future[HandlerResult[ResponseGetFileUploadPartUrl]] =
    authorized(clientData) { client ⇒
      (for {
        file ← fromFutureOption(Errors.FileNotFound)(db.run(persist.FileRepo.findByKey(fsAdapter.parseKey(keyBytes).key)))
        partKeyUrl ← fromFuture(fsAdapter.getFileUploadPartUrl(file.id, partNumber))
        (partKey, url) = partKeyUrl
        _ ← fromFuture(db.run(persist.FilePartRepo.createOrUpdate(file.id, partNumber, partSize, partKey.key)))
      } yield ResponseGetFileUploadPartUrl(url)).value map (_.toScalaz)
    }

  override def jhandleCommitFileUpload(keyBytes: Array[Byte], fileName: String, clientData: ClientData): Future[HandlerResult[ResponseCommitFileUpload]] =
    authorized(clientData) { client ⇒
      (for {
        file ← fromFutureOption(Errors.FileNotFound)(db.run(persist.FileRepo.findByKey(fsAdapter.parseKey(keyBytes).key)))
        partNames ← fromFuture(db.run(persist.FilePartRepo.findByFileId(file.id) map (_.map(_.uploadKey))))
        _ ← fromFuture(fsAdapter.completeFileUpload(file.id, file.size, fileName, partNames))
        _ ← fromFuture(db.run(persist.FileRepo.setUploaded(file.id, fileName)))
      } yield ResponseCommitFileUpload(ApiFileLocation(file.id, ACLUtils.fileAccessHash(file.id, file.accessSalt)))).value map (_.toScalaz)
    }

}
