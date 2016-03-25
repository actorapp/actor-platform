package im.actor.server.api.rpc

import akka.actor._
import im.actor.api.rpc.Service

final class RpcApiExtension(system: ExtendedActorSystem) extends Extension {
  private var _services = Seq.empty[Service]
  private var _chain = buildChain

  def services = _services

  def chain = _chain

  def register(clazz: Class[_ <: Service]): Unit = {
    val service = system.dynamicAccess.createInstanceFor[Service](clazz, List(classOf[ActorSystem] → system)).get
    register(service)
  }

  def register(service: Service): Unit = {
    synchronized {
      _services = _services :+ service
      _chain = buildChain
    }
  }

  def register(services: Seq[Service]): Unit = {
    synchronized {
      this._services = this._services ++ services
      _chain = buildChain
    }
  }

  private def buildChain =
    if (_services.nonEmpty)
      _services.map(_.handleRequestPartial).reduce(_ orElse _)
    else PartialFunction.empty
}

object RpcApiExtension extends ExtensionId[RpcApiExtension] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): RpcApiExtension = new RpcApiExtension(system)

  override def lookup(): ExtensionId[_ <: Extension] = RpcApiExtension
}
