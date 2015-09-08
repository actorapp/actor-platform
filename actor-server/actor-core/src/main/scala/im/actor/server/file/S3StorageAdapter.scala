package im.actor.server.file

import java.io.File

import akka.actor._
import com.amazonaws.HttpMethod
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.transfer.TransferManager
import com.amazonaws.services.s3.transfer.model.UploadResult
import com.github.dwhjames.awswrap.s3.{ AmazonS3ScalaClient, FutureTransfer }
import com.github.kxbmap.configs._
import com.typesafe.config.{ Config, ConfigFactory }
import im.actor.api.rpc.files.ApiFileLocation
import im.actor.serialization.ActorSerializer
import im.actor.server.acl.ACLUtils
import im.actor.server.db.DbExtension
import im.actor.server.{ models, persist }
import slick.driver.PostgresDriver.api._

import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try

class S3StorageExtensionImpl(val s3StorageAdapter: S3StorageAdapter) extends Extension {
  // TODO: move to a proper place

  ActorSerializer.register(80001, classOf[FileLocation])
  ActorSerializer.register(80002, classOf[AvatarImage])
  ActorSerializer.register(80003, classOf[Avatar])
}

object S3StorageExtension extends ExtensionId[S3StorageExtensionImpl] with ExtensionIdProvider {
  override def lookup = S3StorageExtension

  override def createExtension(system: ExtendedActorSystem) = {
    val config = S3StorageAdapterConfig.load(system.settings.config.getConfig("services.aws.s3")).get
    new S3StorageExtensionImpl(new S3StorageAdapter(config, system))
  }
}

case class S3StorageAdapterConfig(bucketName: String, key: String, secret: String)

object S3StorageAdapterConfig {
  def load(config: Config): Try[S3StorageAdapterConfig] = {
    for {
      bucketName ← config.get[Try[String]]("default-bucket")
      key ← config.get[Try[String]]("access-key")
      secret ← config.get[Try[String]]("secret-key")
    } yield S3StorageAdapterConfig(bucketName, key, secret)
  }

  def load: Try[S3StorageAdapterConfig] = {
    val config = ConfigFactory.load().getConfig("services.aws.s3")
    load(config)
  }
}

class S3StorageAdapter(config: S3StorageAdapterConfig, _system: ActorSystem) extends FileStorageAdapter {
  val bucketName = config.bucketName

  private implicit val system: ActorSystem = _system
  private implicit val ec: ExecutionContext = system.dispatcher

  private val awsCredentials = new BasicAWSCredentials(config.key, config.secret)
  private val db = DbExtension(system).db

  val s3Client = new AmazonS3ScalaClient(awsCredentials)
  val transferManager = new TransferManager(awsCredentials)

  override def uploadFile(name: String, file: File): DBIO[ApiFileLocation] =
    uploadFile(bucketName, name, file)

  override def uploadFileF(name: String, file: File): Future[ApiFileLocation] =
    db.run(uploadFile(name, file))

  override def downloadFile(id: Long): DBIO[Option[File]] = {
    persist.File.find(id) flatMap {
      case Some(file) ⇒
        downloadFile(bucketName, file.id, file.name) map (Some(_))
      case None ⇒ DBIO.successful(None)
    }
  }

  override def downloadFileF(id: Long): Future[Option[File]] =
    db.run(downloadFile(id))

  override def getFileUrl(file: models.File, accessHash: Long): Future[Option[String]] = {
    val timeout = 1.day

    if (ACLUtils.fileAccessHash(file.id, file.accessSalt) == accessHash) {
      val presignedRequest = new GeneratePresignedUrlRequest(bucketName, FileUtils.s3Key(file.id, file.name))

      val expiration = new java.util.Date
      expiration.setTime(expiration.getTime + timeout.toMillis)
      presignedRequest.setExpiration(expiration)
      presignedRequest.setMethod(HttpMethod.GET)

      s3Client.generatePresignedUrlRequest(presignedRequest).map(_.toString).map(Some(_))
    } else Future.successful(None)
  }

  private def downloadFile(bucketName: String, id: Long, name: String) = {
    for {
      dirFile ← DBIO.from(FileUtils.createTempDir())
      file = dirFile.toPath.resolve("file").toFile
      _ ← DBIO.from(FutureTransfer.listenFor(transferManager.download(bucketName, FileUtils.s3Key(id, name), file)) map (_.waitForCompletion()))
    } yield file
  }

  private def uploadFile(bucketName: String, name: String, file: File): DBIO[ApiFileLocation] = {
    val rnd = ThreadLocalRandom.current()
    val id = rnd.nextLong()
    val accessSalt = ACLUtils.nextAccessSalt(rnd)
    val sizeF = FileUtils.getFileLength(file)

    for {
      _ ← persist.File.create(id, accessSalt, FileUtils.s3Key(id, name))
      _ ← DBIO.from(s3Upload(bucketName, id, name, file))
      _ ← DBIO.from(sizeF) flatMap (s ⇒ persist.File.setUploaded(id, s, name))
    } yield ApiFileLocation(id, ACLUtils.fileAccessHash(id, accessSalt))
  }

  private def s3Upload(bucketName: String, id: Long, name: String, file: File): Future[UploadResult] = {
    FutureTransfer.listenFor(transferManager.upload(bucketName, FileUtils.s3Key(id, name), file)) map (_.waitForUploadResult())
  }
}