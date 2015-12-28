package im.actor.rpc.raw

import akka.actor.ActorSystem
import cats.data.Xor
import im.actor.api.rpc.collections.ApiRawValue
import im.actor.api.rpc.{ AuthorizedClientData, RpcError }

import scala.concurrent.Future

/**
 * Base class for raw service handlers.
 */
abstract class RawServiceHandler(system: ActorSystem) {

  type RawFunction = AuthorizedClientData ⇒ RawAuthorizedFunction

  type RawAuthorizedFunction = Option[ApiRawValue] ⇒ Future[RpcError Xor ApiRawValue]

  def handle: PartialFunction[String, RawFunction]

}