package im.actor.server.dialog.group

import akka.actor._

sealed trait GroupDialogExtension extends Extension

final class GroupDialogExtensionImpl(system: ActorSystem) extends GroupDialogExtension {
  GroupDialog.register()

  lazy val region: GroupDialogRegion = GroupDialogRegion.start()(system)
}

object GroupDialogExtension extends ExtensionId[GroupDialogExtensionImpl] with ExtensionIdProvider {
  override def lookup = GroupDialogExtension

  override def createExtension(system: ExtendedActorSystem) = new GroupDialogExtensionImpl(system)
}