package im.actor.api.rpc

import cats.data.Xor
import im.actor.server.acl.ACLUtils

import scala.concurrent.ExecutionContext
import akka.actor.ActorSystem
import slick.dbio.DBIO
import im.actor.api.rpc.files.ApiFileLocation
import im.actor.server.persist.files.FileRepo

object FileRpcErrors {
  val FileNotFound = RpcError(404, "FILE_NOT_FOUND", "File not found.", false, None)
  val FileTooLarge = RpcError(400, "FILE_TOO_LARGE", "File is too large.", false, None)
  val LocationInvalid = RpcError(400, "LOCATION_INVALID", "", false, None)
  val UnsupportedSignatureAlgorithm = RpcError(400, "SIGNATURE_ALGO_NOT_SUPPORTED", "", false, None)
}

object FileHelpers {

  def withFileLocation[R <: RpcResponse](fileLocation: ApiFileLocation, maxSize: Long)(f: ⇒ DBIO[RpcError Xor R])(implicit ec: ExecutionContext, s: ActorSystem) = {
    FileRepo.find(fileLocation.fileId) flatMap {
      case Some(file) ⇒
        if (!file.isUploaded) {
          DBIO.successful(Error(FileRpcErrors.LocationInvalid))
        } else if (file.size > maxSize) {
          DBIO.successful(Error(FileRpcErrors.FileTooLarge))
        } else if (ACLUtils.fileAccessHash(file.id, file.accessSalt) != fileLocation.accessHash) {
          DBIO.successful(Error(FileRpcErrors.LocationInvalid))
        } else {
          f
        }
      case None ⇒ DBIO.successful(Error(FileRpcErrors.FileNotFound))
    }
  }

}
