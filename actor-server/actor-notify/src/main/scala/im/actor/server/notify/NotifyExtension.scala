package im.actor.server.notify

import akka.actor.{ ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider }

sealed trait NotifyExtension extends Extension

final class NotifyExtensionImpl(system: ActorSystem) extends NotifyExtension {
  NotifyProcessor.register()
  val notifyProxy = NotifyProcessor.startSingleton()(system)
}

object NotifyExtension extends ExtensionId[NotifyExtensionImpl] with ExtensionIdProvider {
  override def lookup = NotifyExtension

  override def createExtension(system: ExtendedActorSystem) = new NotifyExtensionImpl(system)
}