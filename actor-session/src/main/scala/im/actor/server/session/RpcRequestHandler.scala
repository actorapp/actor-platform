package im.actor.server.session

import akka.actor._
import akka.stream.actor._

import im.actor.server.api.rpc.RpcApiService
import im.actor.server.mtproto.protocol.RpcResponseBox

import scala.concurrent.duration._
import scala.collection.immutable
import scala.language.postfixOps
import scala.util.{ Success, Failure }

import scodec.bits._

object RpcRequestHandler {
  private[session] def props(
    rpcApiService:      ActorRef,
    rpcResponseManager: ActorRef
  ) = Props(classOf[RpcRequestHandler], rpcApiService, rpcResponseManager)
}

class RpcRequestHandler(rpcApiService: ActorRef, rpcResponsePublisher: ActorRef) extends ActorSubscriber with ActorLogging {
  import ActorSubscriberMessage._
  import SessionStream._

  implicit val ec = context.dispatcher

  val MaxRequestQueueSize = 10 // TODO: configurable
  var requestQueue = Map.empty[Long, BitVector]

  override val requestStrategy = new MaxInFlightRequestStrategy(max = MaxRequestQueueSize) {
    override def inFlightInternally: Int = requestQueue.size
  }

  def receive = {
    case OnNext(HandleRpcRequest(messageId, requestBytes)) =>
      requestQueue += (messageId -> requestBytes)
      assert(requestQueue.size <= MaxRequestQueueSize, s"queued too many: ${requestQueue.size}")
      rpcApiService ! RpcApiService.HandleRpcRequest(messageId, requestBytes)
    case RpcApiService.RpcResponse(messageId, responseBytes) =>
      requestQueue -= messageId

      rpcResponsePublisher ! RpcResponseBox(messageId, responseBytes)
    case OnError(cause) =>
      log.error(cause, "Received OnError, sending PoisonPill to ResponseManager")
      rpcResponsePublisher ! PoisonPill
  }
}
