package im.actor.server.user

import akka.actor._

sealed trait UserExtension extends Extension

final class UserExtensionImpl(system: ActorSystem) extends UserExtension {
  UserProcessor.register()

  lazy val processorRegion: UserProcessorRegion = UserProcessorRegion.start()(system)
  lazy val viewRegion: UserViewRegion = UserViewRegion(processorRegion.ref)
}

object UserExtension extends ExtensionId[UserExtensionImpl] with ExtensionIdProvider {
  override def lookup = UserExtension

  override def createExtension(system: ExtendedActorSystem) = new UserExtensionImpl(system)
}