package im.actor.server.dialog.privat

import akka.actor._

sealed trait PrivateDialogExtension extends Extension

final class PrivateDialogExtensionImpl(system: ActorSystem) extends PrivateDialogExtension {
  PrivateDialog.register()

  lazy val region: PrivateDialogRegion = PrivateDialogRegion.start()(system)
}

object PrivateDialogExtension extends ExtensionId[PrivateDialogExtensionImpl] with ExtensionIdProvider {
  override def lookup = PrivateDialogExtension

  override def createExtension(system: ExtendedActorSystem) = new PrivateDialogExtensionImpl(system)
}