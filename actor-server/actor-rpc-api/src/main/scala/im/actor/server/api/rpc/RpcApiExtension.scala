package im.actor.server.api.rpc

import akka.actor._
import im.actor.api.rpc.Service
import im.actor.server.api.rpc.RpcApiService.RefreshChain

final class RpcApiExtension(system: ExtendedActorSystem) extends Extension {
  val serviceRef = system.actorOf(RpcApiService.props(Seq.empty), "rpc-api-service")

  private[rpc] var services = Seq.empty[Service]

  def register(clazz: Class[_ <: Service]): Unit = {
    val service = system.dynamicAccess.createInstanceFor[Service](clazz, List(classOf[ActorSystem] → system)).get
    register(service)
  }

  def register(service: Service): Unit = {
    synchronized {
      services = services :+ service
    }

    serviceRef ! RefreshChain
  }

  def register(services: Seq[Service]): Unit = {
    synchronized {
      this.services = this.services ++ services
    }

    serviceRef ! RefreshChain
  }

  private[rpc] def getChain = services.map(_.handleRequestPartial).reduce(_ orElse _)
}

object RpcApiExtension extends ExtensionId[RpcApiExtension] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): RpcApiExtension = new RpcApiExtension(system)

  override def lookup(): ExtensionId[_ <: Extension] = RpcApiExtension
}
