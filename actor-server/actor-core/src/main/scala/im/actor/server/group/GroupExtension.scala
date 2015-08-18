package im.actor.server.group

import akka.actor._

sealed trait GroupExtension extends Extension

final class GroupExtensionImpl(system: ActorSystem) extends GroupExtension {
  GroupProcessor.register()

  lazy val processorRegion: GroupProcessorRegion = GroupProcessorRegion.start()(system)
  lazy val viewRegion: GroupViewRegion = GroupViewRegion(processorRegion.ref)
}

object GroupExtension extends ExtensionId[GroupExtensionImpl] with ExtensionIdProvider {
  override def lookup = GroupExtension

  override def createExtension(system: ExtendedActorSystem) = new GroupExtensionImpl(system)
}