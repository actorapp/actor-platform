package im.actor.server

import akka.actor.ActorSystem
import com.typesafe.config.{ ConfigException, ConfigObject, Config }
import im.actor.server.api.rpc.RpcApiExtension

import scala.collection.JavaConversions._
import scala.util.{ Failure, Success, Try }

private[server] trait ActorServerModules {
  private val RpcServiceClazz = classOf[im.actor.api.rpc.Service]
  private val AkkaExtensionClazz = classOf[akka.actor.Extension]

  protected def startModules(system: ActorSystem): Unit =
    system.settings.config.getObject("modules") foreach {
      case (n, c: ConfigObject) ⇒ startModule(system, n, c.toConfig)
      case (_, c) ⇒
        throw new RuntimeException(s"Module have to be a config but got: ${c.getClass.getName}")
    }

  private def startModule(system: ActorSystem, name: String, config: Config): Unit = {
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
      clazz ← Try(Class.forName(fqcn).asSubclass(AkkaExtensionClazz))
    } yield clazz.getDeclaredConstructor(classOf[ActorSystem]).newInstance(system)) match {
      case Success(_)                          ⇒
      case Failure(_: ConfigException.Missing) ⇒
      case Failure(_: ClassCastException) ⇒
        throw new RuntimeException(s"extension should extend akka.actor.Extension")
      case Failure(e) ⇒ throw e
    }
  }
}