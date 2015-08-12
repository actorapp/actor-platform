package im.actor.server.dialog.pair

import akka.actor._

sealed trait PairDialogExtension extends Extension

final class PairDialogExtensionImpl(system: ActorSystem) extends PairDialogExtension {
  lazy val region: PairDialogRegion = PairDialogRegion.start()(system)
}

object PairDialogExtension extends ExtensionId[PairDialogExtensionImpl] with ExtensionIdProvider {
  override def lookup = PairDialogExtension

  override def createExtension(system: ExtendedActorSystem) = new PairDialogExtensionImpl(system)
}