package im.actor.api.rpc.raw

import akka.actor.ActorSystem
import cats.data.Xor
import im.actor.api.rpc.collections._
import im.actor.api.rpc.{ AuthorizedClientData, RpcError }

import scala.concurrent.Future

// todo: find a better name. It is not array style already. Arrays just could be parsed to case classes for convinience
abstract class ArrayStyleRawApiService(system: ActorSystem) extends RawApiService(system) with RawValueParserTypeclassInstances {
  import im.actor.api.rpc.FutureResultRpcCats._
  import system.dispatcher

  type Request

  final override def handleRequests: Handler = implicit client ⇒ params ⇒ new PartialFunction[String, Future[Response]] {
    override def isDefinedAt(name: String): Boolean = validateRequests(None).isDefinedAt(name)

    override def apply(name: String): Future[Response] = {
      (for {
        request ← fromEither(validateRequests(params)(name))
        result ← fromFutureEither(processRequests(client)(request))
      } yield result).value
    }
  }

  protected def validateRequests: Option[ApiRawValue] ⇒ PartialFunction[String, RpcError Xor Request]

  protected def processRequests: AuthorizedClientData ⇒ PartialFunction[Request, Future[Response]]

  /**
   * Parse content of `optParams` to type T,
   * Returns `RawApiRpcErrors.InvalidParams` if `optParams` is empty
   * Returns `RawApiRpcErrors.InvalidParams` if it wasn't able to parse `optParams` to type T
   * @param optParams data that will be parsed. Should be non empty
   * @tparam T type to parse to
   */
  final protected def parseParams[T: RawValueParser](optParams: Option[ApiRawValue]): RpcError Xor T =
    for {
      params ← Xor.fromOption(optParams, RawApiRpcErrors.InvalidParams)
      result ← Xor.fromOption(RawValueParser.parse[T](params), RawApiRpcErrors.InvalidParams)
    } yield result

}
