package im.actor.server.user

import akka.actor._
import akka.util.Timeout

import scala.concurrent.duration._

sealed trait UserExtension extends Extension

final class UserExtensionImpl(actorSystem: ActorSystem) extends UserExtension with UserOperations {
  UserProcessor.register()

  implicit val system = actorSystem

  lazy val processorRegion: UserProcessorRegion = UserProcessorRegion.start()(system)
  lazy val viewRegion: UserViewRegion = UserViewRegion(processorRegion.ref)

  implicit val timeout: Timeout = Timeout(20.seconds)
}

object UserExtension extends ExtensionId[UserExtensionImpl] with ExtensionIdProvider {
  override def lookup = UserExtension

  override def createExtension(system: ExtendedActorSystem) = new UserExtensionImpl(system)
}