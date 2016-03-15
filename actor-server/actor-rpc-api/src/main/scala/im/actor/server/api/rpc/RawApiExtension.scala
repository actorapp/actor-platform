package im.actor.server.api.rpc

import akka.actor._
import cats.data.Xor
import im.actor.api.rpc.collections.ApiRawValue
import im.actor.api.rpc.FutureResultRpc
import im.actor.api.rpc.raw.RawApiService
import im.actor.api.rpc.{ AuthorizedClientData, ClientData, CommonRpcErrors, RpcError }

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

sealed trait RawApiExtension extends Extension

private[rpc] final class RawApiExtensionImpl(system: ExtendedActorSystem) extends RawApiExtension {
  import FutureResultRpc._
  import system.dispatcher

  private val services = TrieMap.empty[String, RawApiService]

  def register(name: String, clazz: Class[_ <: RawApiService]): Unit = {
    val service = system.dynamicAccess.createInstanceFor[RawApiService](clazz, List(classOf[ActorSystem] → system)).get
    register(name, service)
  }

  def register(name: String, service: RawApiService): Unit = services.putIfAbsent(name, service)

  def register(serviceSeq: Seq[(String, RawApiService)]): Unit = services ++= serviceSeq

  def handle(service: String, method: String, params: Option[ApiRawValue], clientData: ClientData): Future[RpcError Xor ApiRawValue] =
    (for {
      serviceHandler ← fromOption(CommonRpcErrors.UnsupportedRequest)(services.get(service))
      response ← fromOption(CommonRpcErrors.UnsupportedRequest)(serviceHandler.handleRequests(clientData)(params).lift(method))
      result ← fromFutureXor(response)
    } yield result).value
}

object RawApiExtension extends ExtensionId[RawApiExtensionImpl] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem) = new RawApiExtensionImpl(system)

  override def lookup(): ExtensionId[_ <: Extension] = RawApiExtension
}
