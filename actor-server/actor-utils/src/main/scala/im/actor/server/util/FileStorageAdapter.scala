package im.actor.server.util

import java.io.File

import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent._

import akka.actor.ActorSystem
import com.amazonaws.services.s3.transfer.TransferManager
import com.github.dwhjames.awswrap.s3.FutureTransfer
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.files.FileLocation
import im.actor.server.persist

trait FileStorageAdapter extends UploadActions

private[util] trait UploadActions {
  def uploadFile(name: String, file: File): DBIO[FileLocation]

  def uploadFileF(name: String, file: File): Future[FileLocation]
}

object FileStorageAdapter {
  /**
   * Constructs default UploadManager (S3)
   *
   * @param bucketName
   * @return S3 Upload Manager
   */
  def apply(bucketName: String)(
    implicit
    db:              Database,
    system:          ActorSystem,
    transferManager: TransferManager
  ): FileStorageAdapter = new S3StorageAdapter(bucketName)
}

class S3StorageAdapter(bucketName: String)(
  implicit
  db:              Database,
  system:          ActorSystem,
  transferManager: TransferManager
) extends FileStorageAdapter {
  private implicit val ec: ExecutionContext = system.dispatcher

  override def uploadFile(name: String, file: File): DBIO[FileLocation] =
    uploadFile(bucketName, name, file)

  override def uploadFileF(name: String, file: File): Future[FileLocation] =
    db.run(uploadFile(name, file))

  def uploadFile(bucketName: String, name: String, file: File): DBIO[FileLocation] = {
    val rnd = ThreadLocalRandom.current()
    val id = rnd.nextLong()
    val accessSalt = ACLUtils.nextAccessSalt(rnd)
    val sizeF = FileUtils.getFileLength(file)

    for {
      _ ← persist.File.create(id, accessSalt, FileUtils.s3Key(id, name))
      _ ← DBIO.from(s3Upload(bucketName, id, name, file))
      _ ← DBIO.from(sizeF) flatMap (s ⇒ persist.File.setUploaded(id, s, name))
    } yield FileLocation(id, ACLUtils.fileAccessHash(id, accessSalt))
  }

  private def s3Upload(bucketName: String, id: Long, name: String, file: File) = {
    FutureTransfer.listenFor(transferManager.upload(bucketName, FileUtils.s3Key(id, name), file)) map (_.waitForUploadResult())
  }
}