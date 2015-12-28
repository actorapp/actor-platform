package im.actor.server.api.rpc

import akka.actor._
import cats.data.Xor
import im.actor.api.rpc.{ AuthorizedClientData, CommonErrors, RpcError }
import im.actor.rpc.raw.RawServiceHandler

import scala.collection.concurrent.TrieMap

sealed trait RawApiExtension extends Extension

private[rpc] final class RawApiExtensionImpl(system: ExtendedActorSystem) extends RawApiExtension {

  private val services = TrieMap.empty[String, RawServiceHandler]

  def register(name: String, clazz: Class[_ <: RawServiceHandler]): Unit = {
    val service = system.dynamicAccess.createInstanceFor[RawServiceHandler](clazz, List(classOf[ActorSystem] → system)).get
    register(name, service)
  }

  def register(name: String, service: RawServiceHandler): Unit = services.putIfAbsent(name, service)

  def register(serviceSeq: Seq[(String, RawServiceHandler)]): Unit = services ++= serviceSeq

  def getHandlingFunction(service: String, method: String)(implicit client: AuthorizedClientData): RpcError Xor RawServiceHandler#RawAuthorizedFunction = {
    val optFunction: Option[RawServiceHandler#RawAuthorizedFunction] =
      for {
        handler ← services.get(service)
        rawFunc ← handler.handle.lift(method)
      } yield rawFunc(client)
    Xor.fromOption(optFunction, CommonErrors.UnsupportedRequest)
  }
}

object RawApiExtension extends ExtensionId[RawApiExtensionImpl] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem) = new RawApiExtensionImpl(system)

  override def lookup(): ExtensionId[_ <: Extension] = RawApiExtension
}
