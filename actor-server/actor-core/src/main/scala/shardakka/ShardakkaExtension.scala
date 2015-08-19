package shardakka

import akka.actor._
import shardakka.keyvalue.SimpleKeyValueExtension
import scala.concurrent.duration._

trait ShardakkaExtension extends Extension with SimpleKeyValueExtension

final class ShardakkaExtensionImpl(_system: ExtendedActorSystem) extends ShardakkaExtension {
  private implicit val system: ActorSystem = _system

}

object ShardakkaExtension extends ExtensionId[ShardakkaExtension] with ExtensionIdProvider {
  val CacheTTL = 5.minutes
  val KVPersistencePrefix = "kv"

  override def createExtension(system: ExtendedActorSystem): ShardakkaExtension = new ShardakkaExtensionImpl(system)

  override def lookup(): ExtensionId[_ <: Extension] = ShardakkaExtension
}
