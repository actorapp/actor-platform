package im.actor.rpc.raw

import akka.actor.ActorSystem
import cats.data.Xor
import im.actor.api.rpc.collections.ApiRawValue
import im.actor.api.rpc.{ AuthorizedClientData, RpcError }

import scala.concurrent.Future

/**
 * Base class for raw service handlers.
 */
abstract class RawApiService(system: ActorSystem) {

  type Response = Future[RpcError Xor ApiRawValue]

  type Handler = AuthorizedClientData ⇒ Option[ApiRawValue] ⇒ PartialFunction[String, Response]

  def handleRequests: Handler

}