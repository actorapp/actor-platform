package im.actor.server.file.local

import java.io.IOException
import java.net.URLEncoder
import java.time.{ Duration, Instant }

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ HttpMethods, Uri }
import akka.stream.{ ActorMaterializer, Materializer }
import better.files._
import im.actor.acl.ACLFiles
import im.actor.server.api.http.{ HttpApi, HttpApiConfig }
import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.db.DbExtension
import im.actor.server.file._
import im.actor.server.model.{ File ⇒ FileModel }
import im.actor.server.file.local.http.FilesHttpHandler
import im.actor.server.persist.files.FileRepo
import im.actor.util.ThreadLocalSecureRandom

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

/**
 * File adapter that works with local file system to store and retrieve files
 * To use this file adapter as default provide its FQCN in modules.files.adapter of your server config.
 * You also need to provide default file location in services.file-storage.location of your server config
 * On initialization it will try to create default file location, if it does not exist and write simple text file
 * to check user's read/write permissions
 *
 * @param _system actor system
 */
final class LocalFileStorageAdapter(_system: ActorSystem)
  extends FileStorageAdapter
  with RequestSigning
  with FileStorageOperations
  with LocalUploadKeyImplicits {

  protected implicit val system: ActorSystem = _system
  protected implicit val ec: ExecutionContext = system.dispatcher
  protected implicit val mat: Materializer = ActorMaterializer()

  private val db = DbExtension(system).db

  private val httpConfig: HttpApiConfig = HttpApiConfig.load.get
  private val storageConfig: LocalFileStorageConfig = LocalFileStorageConfig.load.get

  private val httpHandler = new FilesHttpHandler(storageConfig)
  HttpApi(system).registerRoute("localstorage") { _ ⇒ httpHandler.routes }
  HttpApi(system).registerRejection("localstorage") { _ ⇒ httpHandler.rejectionHandler }

  protected val storageLocation = initFileStorage(storageConfig.location)

  /**
   * Initializes local file storage, and performs check that user have read/write permissions on file system
   *
   * @param location file storage location
   * @return same location as passed parameter if check succeeds
   */
  def initFileStorage(location: String): String = {
    val initMessage = "Actor local file storage initialized."
    (for {
      storageDir ← Try(file"$location".createIfNotExists(asDirectory = true))
      _ ← Try((storageDir / "init").createIfNotExists() < initMessage)
    } yield ()) match {
      case Success(_) ⇒ location
      case Failure(e: IOException) ⇒
        system.log.error(e, "Failed to initialize local file storage. You should provide correct path to directory with read and write permissions for user `actor`")
        println("Failed to initialize local file storage. It is probably issue with read/write permissions on file system")
        throw new RuntimeException("Failed to initialize local file storage", e)
      case Failure(e) ⇒
        system.log.error(e, "Failed to initialize local file storage.")
        println("Failed to initialize local file storage.")
        throw new RuntimeException("Failed to initialize local file storage", e)
    }
  }

  val baseUri = Uri(httpConfig.baseUri)

  override def uploadFile(name: UnsafeFileName, data: Array[Byte]): DBIO[FileLocation] = {
    val rng = ThreadLocalSecureRandom.current()
    val id = ACLFiles.randomLong(rng)
    val accessSalt = ACLFiles.nextAccessSalt(rng)

    val size = data.length

    for {
      _ ← FileRepo.create(id, size.toLong, accessSalt, LocalUploadKey.fileKey(id).key)
      _ ← DBIO.from(createFile(id, name.safe, data))
      _ ← FileRepo.setUploaded(id, name.safe)
    } yield FileLocation(id, ACLFiles.fileAccessHash(id, accessSalt))

  }

  override def uploadFileF(name: UnsafeFileName, data: Array[Byte]): Future[FileLocation] = db.run(uploadFile(name, data))

  /**
   * Generates upload uri similar to:
   * https://api.actor.im/v1/files/:fileId?expires=:expiresAt&signature=:signature
   *
   * @param fileId uploaded file id
   * @return file upload uri
   */
  override def getFileUploadUrl(fileId: Long): Future[(UploadKey, String)] = {
    val query = baseUri
      .withPath(Uri.Path(s"/v1/files/$fileId"))
      .withQuery(Uri.Query("expires" → expiresAt().toString))
    Future.successful(LocalUploadKey.fileKey(fileId) → signRequest(HttpMethods.PUT, query, ACLFiles.secretKey()).toString)
  }

  override def completeFileUpload(fileId: Long, fileSize: Long, fileName: UnsafeFileName, partNames: Seq[String]): Future[Unit] = {
    val fileDir = fileDirectory(fileId)
    for {
      isComplete ← haveAllParts(fileDir, partNames, fileSize)
      result ← concatFiles(fileDir, partNames, fileName.safe, fileSize)
      _ ← if (isComplete) deleteUploadedParts(fileDir, partNames) else Future.successful(())
      _ ← db.run(FileRepo.setUploaded(fileId, fileName.safe))
    } yield ()
  }

  override def downloadFile(id: Long): DBIO[Option[Array[Byte]]] = DBIO.from(downloadFileF(id))

  override def downloadFileF(id: Long): Future[Option[Array[Byte]]] = getFileData(id) map (_ map (_.toArray))

  /**
   * Generates download uri similar to:
   * https://api.actor.im/v1/files/:fileId/fileName?expires=:expiresAt&signature=:signature
   * or if filename not present:
   * https://api.actor.im/v1/files/:fileId?expires=:expiresAt&signature=:signature
   *
   * @param file file model
   * @param accessHash file access hash
   * @return file download uri
   */
  override def getFileDownloadUrl(file: FileModel, accessHash: Long): Future[Option[String]] = {
    if (ACLFiles.fileAccessHash(file.id, file.accessSalt) == accessHash) {
      val filePart = Option(file.name) filter (_.trim.nonEmpty) map (n ⇒ s"/${URLEncoder.encode(n, "UTF-8")}") getOrElse ""
      val query = baseUri
        .withPath(Uri.Path(s"/v1/files/${file.id}" + filePart))
        .withQuery(Uri.Query("expires" → expiresAt().toString))
      val signedRequest = signRequest(HttpMethods.GET, query, ACLFiles.secretKey()).toString
      Future.successful(Some(signedRequest))
    } else Future.successful(None)
  }

  /**
   * Generates upload uri for parts similar to:
   * https://api.actor.im/v1/files/:fileId/:partNumber?expires=:expiresAt&signature=:signature
   *
   * @param fileId file id
   * @param partNumber part number
   * @return file part upload uri
   */
  override def getFileUploadPartUrl(fileId: Long, partNumber: Int): Future[(UploadKey, String)] = {
    val query =
      baseUri
        .withPath(Uri.Path(s"/v1/files/$fileId/$partNumber"))
        .withQuery(Uri.Query("expires" → expiresAt().toString))
    Future.successful(LocalUploadKey.partKey(fileId, partNumber) → signRequest(HttpMethods.PUT, query, ACLFiles.secretKey()).toString)
  }

  def parseKey(bytes: Array[Byte]): UploadKey = LocalUploadKey.parseFrom(bytes)

  private def expiresAt(): Long = Instant.now.plus(Duration.ofDays(1)).toEpochMilli

}

