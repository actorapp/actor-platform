package im.actor.api.rpc

import im.actor.server.acl.ACLUtils

import scala.concurrent.ExecutionContext
import scalaz.\/

import akka.actor.ActorSystem
import slick.dbio.DBIO

import im.actor.api.rpc.files.ApiFileLocation
import im.actor.server.persist

object FileHelpers {

  // TODO: move file checks into FileUtils
  object Errors {
    val FileNotFound = RpcError(404, "FILE_NOT_FOUND", "File not found.", false, None)
    val FileTooLarge = RpcError(400, "FILE_TOO_LARGE", "File is too large.", false, None)
    val LocationInvalid = RpcError(400, "LOCATION_INVALID", "", false, None)
  }

  def withFileLocation[R <: RpcResponse](fileLocation: ApiFileLocation, maxSize: Int)(f: ⇒ DBIO[RpcError \/ R])(implicit ec: ExecutionContext, s: ActorSystem) = {
    persist.File.find(fileLocation.fileId) flatMap {
      case Some(file) ⇒
        if (!file.isUploaded) {
          DBIO.successful(Error(Errors.LocationInvalid))
        } else if (file.size > maxSize) {
          DBIO.successful(Error(Errors.FileTooLarge))
        } else if (ACLUtils.fileAccessHash(file.id, file.accessSalt) != fileLocation.accessHash) {
          DBIO.successful(Error(Errors.LocationInvalid))
        } else {
          f
        }
      case None ⇒ DBIO.successful(Error(Errors.FileNotFound))
    }
  }

}
