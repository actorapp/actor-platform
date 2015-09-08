package im.actor.server.api.rpc.service.files

import java.io.{ File, FileOutputStream }
import java.nio.file.Files

import im.actor.server.acl.ACLUtils
import im.actor.server.file.{ FileUtils, S3StorageExtension, S3StorageAdapter }

import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.{ ExecutionContext, Future }

import akka.actor._
import com.amazonaws.HttpMethod
import com.amazonaws.services.s3.model._
import com.amazonaws.services.s3.transfer.TransferManager
import com.github.dwhjames.awswrap.s3.{ AmazonS3ScalaClient, FutureTransfer }
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.FileHelpers.Errors
import im.actor.api.rpc.files._
import im.actor.api.rpc.{ ClientData, _ }
import im.actor.server.db.DbExtension
import im.actor.server.{ models, persist }

class FilesServiceImpl(
  implicit
  actorSystem: ActorSystem
) extends FilesService {

  import scala.collection.JavaConverters._

  import FileUtils._

  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  private implicit val db: Database = DbExtension(actorSystem).db
  private val fsAdapter: S3StorageAdapter = S3StorageExtension(actorSystem).s3StorageAdapter

  override def jhandleGetFileUrl(location: ApiFileLocation, clientData: ClientData): Future[HandlerResult[ResponseGetFileUrl]] = {
    val authorizedAction = requireAuth(clientData) map { client ⇒
      persist.File.find(location.fileId) flatMap {
        implicit val timeout = 1.day

        {
          case Some(file) ⇒
            DBIO.from(fsAdapter.getFileUrl(file, location.accessHash)).map { optUrl ⇒
              optUrl.map { url ⇒
                Ok(ResponseGetFileUrl(url, timeout.toSeconds.toInt))
              }.getOrElse(Error(Errors.LocationInvalid))
            }
          case None ⇒ DBIO.successful(Error(Errors.LocationInvalid))
        }
      }
    }
    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleGetFileUploadUrl(expectedSize: Int, clientData: ClientData): Future[HandlerResult[ResponseGetFileUploadUrl]] = {
    val authorizedAction = requireAuth(clientData).map { client ⇒
      val rnd = ThreadLocalRandom.current()
      val id = rnd.nextLong()
      val salt = ACLUtils.nextAccessSalt(rnd)

      val key = s"upload_${id}"
      val presignedRequest = new GeneratePresignedUrlRequest(fsAdapter.bucketName, key)
      val expiration = new java.util.Date
      expiration.setTime(expiration.getTime + 1.day.toMillis)
      presignedRequest.setExpiration(expiration)
      presignedRequest.setMethod(HttpMethod.PUT)

      for {
        _ ← persist.File.create(id, salt, key)
        url ← DBIO.from(fsAdapter.s3Client.generatePresignedUrlRequest(presignedRequest))
      } yield {
        Ok(ResponseGetFileUploadUrl(url.toString, key.getBytes()))
      }
    }

    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleGetFileUploadPartUrl(partNumber: Int, partSize: Int, uploadKey: Array[Byte], clientData: ClientData): Future[HandlerResult[ResponseGetFileUploadPartUrl]] = {
    val authorizedAction = requireAuth(clientData).map { client ⇒
      val key = new String(uploadKey)

      persist.File.findByKey(key) flatMap {
        case Some(file) ⇒
          val partKey = s"upload_part_${file.s3UploadKey}_${partNumber}"
          val request = new GeneratePresignedUrlRequest(fsAdapter.bucketName, partKey)
          val expiration = new java.util.Date
          expiration.setTime(expiration.getTime + 1.day.toMillis)
          request.setMethod(HttpMethod.PUT)
          request.setExpiration(expiration)
          request.setContentType("application/octet-stream")

          for {
            url ← DBIO.from(fsAdapter.s3Client.generatePresignedUrlRequest(request))
            _ ← persist.FilePart.createOrUpdate(file.id, partNumber, partSize, partKey)
          } yield {
            Ok(ResponseGetFileUploadPartUrl(url.toString))
          }
        case None ⇒
          DBIO.successful(Error(Errors.FileNotFound))
      }
    }

    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleCommitFileUpload(uploadKey: Array[Byte], fileName: String, clientData: ClientData): Future[HandlerResult[ResponseCommitFileUpload]] = {
    val authorizedAction = requireAuth(clientData).map { client ⇒
      val key = new String(uploadKey)

      persist.File.findByKey(key) flatMap {
        case Some(file) ⇒
          for {
            parts ← persist.FilePart.findByFileId(file.id)
            tempDir ← DBIO.from(createTempDir())
            download = FutureTransfer.listenFor {
              fsAdapter.transferManager.downloadDirectory(fsAdapter.bucketName, s"upload_part_${file.s3UploadKey}", tempDir)
            } map (_.waitForCompletion())
            _ ← DBIO.from(download)
            concatFile ← DBIO.from(concatFiles(tempDir, parts map (_.s3UploadKey)))
            fileLengthF = getFileLength(concatFile)
            upload = FutureTransfer.listenFor {
              fsAdapter.transferManager.upload(fsAdapter.bucketName, FileUtils.s3Key(file.id, fileName), concatFile)
            } map (_.waitForCompletion())
            _ ← DBIO.from(upload)
            _ ← DBIO.from(deleteDir(tempDir))
            _ ← DBIO.from(fileLengthF) flatMap (size ⇒ persist.File.setUploaded(file.id, size, fileName))
          } yield {
            Ok(ResponseCommitFileUpload(ApiFileLocation(file.id, ACLUtils.fileAccessHash(file.id, file.accessSalt))))
          }
        case None ⇒
          DBIO.successful(Error(Errors.FileNotFound))
      }
    }

    db.run(toDBIOAction(authorizedAction))
  }

  private def copyPartRequest(part: models.FilePart, destinationUploadId: String, destinationKey: String): CopyPartRequest = {
    (new CopyPartRequest)
      .withDestinationBucketName(fsAdapter.bucketName)
      .withDestinationKey(destinationKey)
      .withSourceBucketName(fsAdapter.bucketName)
      .withSourceKey(part.s3UploadKey)
      .withUploadId(destinationUploadId)
      .withPartNumber(part.number)
  }

  private def etags(responses: Seq[CopyPartResult]): java.util.List[PartETag] = {
    val xs = responses map { response ⇒
      new PartETag(response.getPartNumber(), response.getETag())
    }

    mutable.Seq(xs: _*).asJava
  }

  // FIXME: #perf use nio and pinned dispatcher

  private def concatFiles(dir: File, fileNames: Seq[String]): Future[File] = {
    Future {
      val dirPath = dir.toPath
      val concatFile = dirPath.resolve("concatenated").toFile

      val outStream = new FileOutputStream(concatFile)

      fileNames foreach { fileName ⇒
        Files.copy(dirPath.resolve(fileName), outStream)
      }

      outStream.close()

      concatFile
    }
  }
}
