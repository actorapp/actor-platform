package im.actor.server.dialog

import akka.actor._
import im.actor.server.dialog.group.GroupDialogRegion
import im.actor.server.dialog.privat.PrivateDialogRegion

sealed trait DialogExtension extends Extension

final class DialogExtensionImpl(system: ActorSystem) extends DialogExtension {
  DialogProcessor.register()

  lazy val privateRegion: PrivateDialogRegion = PrivateDialogRegion.start()(system)
  lazy val groupRegion: GroupDialogRegion = GroupDialogRegion.start()(system)
}

object DialogExtension extends ExtensionId[DialogExtensionImpl] with ExtensionIdProvider {
  override def lookup = DialogExtension

  override def createExtension(system: ExtendedActorSystem) = new DialogExtensionImpl(system)
}