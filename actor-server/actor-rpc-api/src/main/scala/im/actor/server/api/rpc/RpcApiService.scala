package im.actor.server.api.rpc

import akka.actor._
import akka.pattern.pipe
import im.actor.api.rpc._
import im.actor.api.rpc.codecs._
import scodec.bits._

import scala.concurrent._
import scalaz._

object RpcApiService {

  @SerialVersionUID(1L)
  final case class HandleRpcRequest(messageId: Long, requestBytes: BitVector, clientData: ClientData)

  @SerialVersionUID(1L)
  final case class RpcResponse(messageId: Long, result: RpcResult)

  @SerialVersionUID(1L)
  private[rpc] case object RefreshChain

  private[rpc] def props(services: Seq[Service]) = Props(classOf[RpcApiService], services)
}

private[rpc] final class RpcApiService(services: Seq[Service]) extends Actor with ActorLogging {

  import RpcApiService._

  private type Chain = PartialFunction[RpcRequest, ClientData ⇒ Future[RpcError \/ RpcOk]]

  // TODO: configurable
  private val DefaultErrorDelay = 5

  private implicit val ec: ExecutionContext = context.dispatcher

  private val rpcApiExt = RpcApiExtension(context.system)
  private var chain: Chain = PartialFunction.empty

  def receive = {
    case RefreshChain ⇒
      chain = rpcApiExt.getChain
    case msg @ HandleRpcRequest(messageId, requestBytes, clientData) ⇒
      val replyTo = sender()

      try {
        RequestCodec.decode(requestBytes).require map {
          case Request(rpcRequest) ⇒
            log.debug("Request: {}, MessageId: {}, Client: {}", rpcRequest, messageId, clientData)

            val result =
              if (chain.isDefinedAt(rpcRequest)) {
                chain(rpcRequest)(clientData)
              } else {
                log.error("Unsupported request {}", rpcRequest)
                Future.successful(Error(CommonRpcErrors.UnsupportedRequest))
              }

            result
              .map { res ⇒
                log.debug("Response: {}, Client: {}", res, clientData)
                res.fold(err ⇒ err, ok ⇒ ok)
              }
              .recover {
                case e: Throwable ⇒
                  log.error(e, "Failed to handle messageId: {} rpcRequest: {}", messageId, rpcRequest)
                  RpcInternalError(true, DefaultErrorDelay)
              }
              .map(result ⇒ RpcResponse(messageId, result))
              .pipeTo(replyTo)
          case _ ⇒
            Future.successful(CommonRpcErrors.UnsupportedRequest)
        }
      } catch {
        case e: Exception ⇒
          log.error(e, "Failure in RpcApiService while handling messageId: {}", messageId)
          replyTo ! RpcResponse(messageId, RpcInternalError(true, DefaultErrorDelay)) // TODO: configurable delay
        case e: Throwable ⇒
          log.error(e, "Failed to handle {}", msg)
      }
  }
}
