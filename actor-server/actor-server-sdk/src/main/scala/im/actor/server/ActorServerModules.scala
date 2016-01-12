package im.actor.server

import akka.actor._
import com.typesafe.config.{ ConfigException, ConfigObject, Config }
import im.actor.server.api.rpc.RpcApiExtension

import scala.collection.JavaConversions._
import scala.util.{ Failure, Success, Try }

object ActorServerModules extends ExtensionId[ActorServerModules] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): ActorServerModules = new ActorServerModules(system)

  override def lookup(): ExtensionId[_ <: Extension] = ActorServerModules
}

final class ActorServerModules(system: ExtendedActorSystem) extends Extension {
  private val RpcServiceClazz = classOf[im.actor.api.rpc.Service]

  def startModules(): Unit =
    system.settings.config.getObject("modules") foreach {
      case (n, c: ConfigObject) ⇒ startModule(n, c.toConfig)
      case (_, c) ⇒
        throw new RuntimeException(s"Module have to be a config but got: ${c.getClass.getName}")
    }

  def startModule(name: String, config: Config): Unit = {
    system.log.debug("Starting module {}", name)

    (for {
      fqcn ← Try(config.getString("rpc"))
      clazz ← Try(Class.forName(fqcn).asSubclass(RpcServiceClazz))
    } yield RpcApiExtension(system).register(clazz)) match {
      case Success(_)                          ⇒
      case Failure(_: ConfigException.Missing) ⇒
      case Failure(_: ClassCastException) ⇒
        throw new RuntimeException(s"rpc should extend im.actor.api.rpc.Service")
      case Failure(e) ⇒ throw e
    }

    (for {
      fqcn ← Try(config.getString("extension"))
      obj ← system.dynamicAccess.getObjectFor[AnyRef](fqcn)
    } yield obj) match {
      case Success(eid: ExtensionId[_]) ⇒ startExtension(eid)
      case Success(_) ⇒
        throw new RuntimeException(s"extension should extend akka.actor.Extension")
      case Failure(_: ConfigException.Missing) ⇒
      case Failure(e)                          ⇒ throw e
    }
  }

  private def startExtension[T <: Extension](ext: ExtensionId[T]): T = ext.apply(system)
}