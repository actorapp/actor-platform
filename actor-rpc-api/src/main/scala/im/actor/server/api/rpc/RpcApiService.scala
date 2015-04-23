package im.actor.server.api.rpc

import scala.concurrent._
import scala.util.Try
import scalaz._

import akka.actor._
import akka.pattern.pipe
import scodec.bits._
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.codecs._

object RpcApiService {

  @SerialVersionUID(1L)
  case class HandleRpcRequest(messageId: Long, requestBytes: BitVector, clientData: ClientData)

  @SerialVersionUID(1L)
  case class RpcResponse(messageId: Long, responseBytes: BitVector)

  @SerialVersionUID(1L)
  case class AttachService(service: Service)

  def props()(implicit db: Database) = Props(classOf[RpcApiService], db)
}

class RpcApiService(implicit db: Database) extends Actor with ActorLogging {

  import RpcApiService._

  private type Chain = PartialFunction[RpcRequest, ClientData ⇒ Future[RpcError \/ RpcOk]]

  implicit private val ec: ExecutionContext = context.dispatcher

  def receive = initialized(Seq.empty)

  def initialized(services: Seq[Service]): Receive = {
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
      case AttachService(service) ⇒
        log.debug("Attached service: {}", service)
        context.become(initialized(services :+ service), discardOld = true)
      case msg @ HandleRpcRequest(messageId, requestBytes, clientData) ⇒
        val replyTo = sender()

        try {
          RequestCodec.decode(requestBytes).require map {
            case Request(rpcRequest) ⇒
              log.debug("Request: {}, Client: {}", rpcRequest, clientData)

              val result =
                if (chain.isDefinedAt(rpcRequest)) {
                  chain(rpcRequest)(clientData)
                } else {
                  log.error("Unsupported request {}", rpcRequest)
                  Future.successful(Error(CommonErrors.UnsupportedRequest))
                }

              result
                .map(_.fold(err ⇒ err, ok ⇒ ok))
                .recover({
                  case e: Throwable ⇒
                    log.error(e, "Failed to handle messageId:{} rpcRequest: {}", messageId, rpcRequest)
                    RpcInternalError(true, 1000) // TODO: configurable delay
                }).map(result ⇒ RpcResponse(messageId, RpcResultCodec.encode(result).require))
                .pipeTo(replyTo)
            case _ ⇒
              Future.successful(CommonErrors.UnsupportedRequest)
          }
        } catch {
          case e: Exception ⇒
            replyTo ! RpcResponse(messageId, RpcResultCodec.encode(RpcInternalError(true, 1000)).require) // TODO: configurable delay
          case e: Throwable ⇒
            log.error(e, "Failed to handle {}", msg)
        }
    }
  }
}
