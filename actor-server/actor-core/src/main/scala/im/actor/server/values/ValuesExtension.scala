package im.actor.server.values

import akka.actor.{ ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider }
import akka.util.Timeout
import im.actor.config.ActorConfig

final class ValuesExtension(val system: ActorSystem) extends Extension with SyncedSet {
  val defaultTimeout = Timeout(ActorConfig.defaultTimeout)
}

object ValuesExtension extends ExtensionId[ValuesExtension] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): ValuesExtension = new ValuesExtension(system)

  override def lookup(): ExtensionId[_ <: Extension] = ValuesExtension
}