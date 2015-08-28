package im.actor.server.api.rpc

import scala.concurrent._
import scalaz._

import akka.actor._
import akka.pattern.pipe
import scodec.bits._

import im.actor.api.rpc._
import im.actor.api.rpc.codecs._

object RpcApiService {

  @SerialVersionUID(1L)
  case class HandleRpcRequest(messageId: Long, requestBytes: BitVector, clientData: ClientData)

  @SerialVersionUID(1L)
  case class RpcResponse(messageId: Long, responseBytes: BitVector)

  def props(services: Seq[Service]) = Props(classOf[RpcApiService], services)
}

final class RpcApiService(services: Seq[Service]) extends Actor with ActorLogging {

  import RpcApiService._

  private type Chain = PartialFunction[RpcRequest, ClientData ⇒ Future[RpcError \/ RpcOk]]

  // TODO: configurable
  private val DefaultErrorDelay = 5

  private implicit val ec: ExecutionContext = context.dispatcher

  def receive: Receive = {
    log.debug("Services list changed: {}", services)

    val chain: Chain =
      if (services.isEmpty) {
        PartialFunction.empty
      } else {
        services.map(_.handleRequestPartial).reduce { (a, b) ⇒
          a.orElse(b)
        }
      }

    {
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
                  Future.successful(Error(CommonErrors.UnsupportedRequest))
                }

              result
                .map { res ⇒
                  log.debug("Response: {}, Client: {}", res, clientData)
                  res.fold(err ⇒ err, ok ⇒ ok)
                }
                .recover {
                  case e: Throwable ⇒
                    log.error(e, "Failed to handle messageId:{} rpcRequest: {}", messageId, rpcRequest)
                    RpcInternalError(true, DefaultErrorDelay)
                }
                .map(result ⇒ RpcResponse(messageId, RpcResultCodec.encode(result).require))
                .pipeTo(replyTo)
            case _ ⇒
              Future.successful(CommonErrors.UnsupportedRequest)
          }
        } catch {
          case e: Exception ⇒
            replyTo ! RpcResponse(messageId, RpcResultCodec.encode(RpcInternalError(true, DefaultErrorDelay)).require) // TODO: configurable delay
          case e: Throwable ⇒
            log.error(e, "Failed to handle {}", msg)
        }
    }
  }
}
