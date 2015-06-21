package im.actor.server.util

import java.io.File

import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.ActorSystem
import com.amazonaws.services.s3.transfer.TransferManager
import slick.dbio
import slick.dbio.Effect.Write
import slick.driver.PostgresDriver
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.files.FileLocation

class UploadManager(bucketName: String)(
  implicit
  db:              Database,
  system:          ActorSystem,
  transferManager: TransferManager
) {
  private implicit val ec: ExecutionContext = system.dispatcher

  def uploadFile(name: String, file: File): Future[FileLocation] = {
    db.run(uploadFileAction(name, file))
  }

  private def uploadFileAction(name: String, file: File): dbio.DBIOAction[FileLocation, PostgresDriver.api.NoStream, Write with PostgresDriver.api.Effect] = {
    FileUtils.uploadFile(bucketName, name, file)
  }
}