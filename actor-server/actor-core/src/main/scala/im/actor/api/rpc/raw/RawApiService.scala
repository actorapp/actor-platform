package im.actor.api.rpc.raw

import akka.actor.ActorSystem
import cats.data.Xor
import im.actor.api.rpc.collections.ApiRawValue
import im.actor.api.rpc.{ AuthorizedClientData, RpcError }

import scala.concurrent.Future

object RawApiRpcErrors {
  val InvalidParams = RpcError(400, "INVALID_PARAMS", "", canTryAgain = true, None)
}

/**
 * Base class for raw service handlers.
 */
abstract class RawApiService(system: ActorSystem) {

  type Response = RpcError Xor ApiRawValue

  type Handler = AuthorizedClientData ⇒ Option[ApiRawValue] ⇒ PartialFunction[String, Future[Response]]

  def handleRequests: Handler

}