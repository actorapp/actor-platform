package im.actor.server.peer

import akka.actor._

sealed trait GroupPeerExtension extends Extension

final class GroupPeerExtensionImpl(system: ActorSystem) extends GroupPeerExtension {
  lazy val region: GroupPeerRegion = GroupPeerRegion.start()(system)
}

object GroupPeerExtension extends ExtensionId[GroupPeerExtensionImpl] with ExtensionIdProvider {
  override def lookup = GroupPeerExtension

  override def createExtension(system: ExtendedActorSystem) = new GroupPeerExtensionImpl(system)
}