package im.actor.server.api.rpc

import akka.actor._
import cats.data.Xor
import im.actor.api.rpc.collections.ApiRawValue
import im.actor.api.rpc.FutureResultRpcCats
import im.actor.api.rpc.{ AuthorizedClientData, CommonErrors, RpcError }
import im.actor.rpc.raw.RawApiService

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

sealed trait RawApiExtension extends Extension

private[rpc] final class RawApiExtensionImpl(system: ExtendedActorSystem) extends RawApiExtension {
  import FutureResultRpcCats._
  import system.dispatcher

  private val services = TrieMap.empty[String, RawApiService]

  def register(name: String, clazz: Class[_ <: RawApiService]): Unit = {
    val service = system.dynamicAccess.createInstanceFor[RawApiService](clazz, List(classOf[ActorSystem] → system)).get
    register(name, service)
  }

  def register(name: String, service: RawApiService): Unit = services.putIfAbsent(name, service)

  def register(serviceSeq: Seq[(String, RawApiService)]): Unit = services ++= serviceSeq

  def handle(service: String, method: String, params: Option[ApiRawValue])(implicit client: AuthorizedClientData): Future[RpcError Xor ApiRawValue] =
    (for {
      serviceHandler ← fromOption(CommonErrors.UnsupportedRequest)(services.get(service))
      response ← fromOption(CommonErrors.UnsupportedRequest)(serviceHandler.handleRequests(client)(params).lift(method))
      result ← fromFutureEither(response)
    } yield result).value
}

object RawApiExtension extends ExtensionId[RawApiExtensionImpl] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem) = new RawApiExtensionImpl(system)

  override def lookup(): ExtensionId[_ <: Extension] = RawApiExtension
}
