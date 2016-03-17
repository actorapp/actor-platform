package im.actor.server.file

import akka.actor._
import im.actor.acl.ACLFiles
import im.actor.config.ActorConfig
import im.actor.serialization.ActorSerializer
import im.actor.server.api.http.HttpApi
import im.actor.server.file.local.LocalUploadKey
import im.actor.server.file.s3.S3UploadKey

import scala.util.{ Failure, Success, Try }

trait FileStorageExtension extends Extension with ACLFiles {
  val fsAdapter: FileStorageAdapter
}

class FileStorageExtensionImpl(system: ActorSystem) extends FileStorageExtension {

  ActorSerializer.register(
    80001 → classOf[FileLocation],
    80002 → classOf[AvatarImage],
    80003 → classOf[Avatar],
    80004 → classOf[S3UploadKey],
    80005 → classOf[LocalUploadKey]
  )

  override val fsAdapter = init()

  private def init(): FileStorageAdapter =
    (for {
      fqcn ← Try(ActorConfig.load().getString("modules.files.adapter"))
      _ = system.log.debug("File adapter is: {}", fqcn)
      clazz ← Try(Class.forName(fqcn).asSubclass(classOf[FileStorageAdapter]))
    } yield clazz.getDeclaredConstructor(classOf[ActorSystem]).newInstance(system)) match {
      case Success(adapter) ⇒
        val httpHandler = new FileUrlBuilderHttpHandler(adapter)(system)
        HttpApi(system).registerRoute("fileurlbuilder") { _ ⇒ httpHandler.routes }
        HttpApi(system).registerRejection("fileurlbuilder") { _ ⇒ httpHandler.rejectionHandler }
        adapter
      case Failure(e) ⇒ throw new RuntimeException("Failed to initialize FileStorageAdapter", e)
    }
}

object FileStorageExtension extends ExtensionId[FileStorageExtensionImpl] with ExtensionIdProvider {
  override def lookup = FileStorageExtension

  override def createExtension(system: ExtendedActorSystem) = new FileStorageExtensionImpl(system)
}