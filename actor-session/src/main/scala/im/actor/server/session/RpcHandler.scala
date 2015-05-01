package im.actor.server.session

import scala.annotation.tailrec
import scala.collection.immutable
import scala.language.postfixOps

import akka.actor._
import akka.stream.actor._
import scodec.bits._

import im.actor.server.api.rpc.RpcApiService
import im.actor.server.mtproto.protocol.{ MessageAck, ProtoMessage, RpcResponseBox }

private[session] object RpcHandler {
  def props(rpcApiService: ActorRef) = Props(classOf[RpcHandler], rpcApiService)
}

private[session] class RpcHandler(rpcApiService: ActorRef) extends ActorSubscriber with ActorPublisher[ProtoMessage] with ActorLogging {

  import ActorPublisherMessage._
  import ActorSubscriberMessage._

  import SessionStreamMessage._

  implicit val ec = context.dispatcher

  def receive = subscriber.orElse(publisher).orElse {
    case unmatched ⇒
      log.error("Unmatched msg {}", unmatched)
  }

  // Subscriber-related

  // TODO: configurable
  private[this] val MaxRequestQueueSize = 10
  private[this] var requestQueue = Map.empty[Long, BitVector]

  def subscriber: Receive = {
    case OnNext(HandleRpcRequest(messageId, requestBytes, clientData)) ⇒
      requestQueue += (messageId → requestBytes)
      assert(requestQueue.size <= MaxRequestQueueSize, s"queued too many: ${requestQueue.size}")

      log.debug("Publishing acknowledge for messageId: {}", messageId)
      enqueueProtoMessage(MessageAck(Vector(messageId)))

      log.debug("Making an rpc request for messageId: {}", messageId)
      rpcApiService ! RpcApiService.HandleRpcRequest(messageId, requestBytes, clientData)
  }

  override val requestStrategy = new MaxInFlightRequestStrategy(max = MaxRequestQueueSize) {
    override def inFlightInternally: Int = requestQueue.size
  }

  // publisher-related
  private[this] var protoMessageQueue = immutable.Queue.empty[ProtoMessage]

  def publisher: Receive = {
    case RpcApiService.RpcResponse(messageId, responseBytes) ⇒
      requestQueue -= messageId

      log.debug("Received RpcResponse for messageId: {}, publishing", messageId)
      enqueueProtoMessage(RpcResponseBox(messageId, responseBytes))
    case Request(_) ⇒
      deliverBuf()
    case Cancel ⇒
      context.stop(self)
  }

  private def enqueueProtoMessage(message: ProtoMessage): Unit = {
    if (protoMessageQueue.isEmpty && totalDemand > 0) {
      onNext(message)
    } else {
      protoMessageQueue = protoMessageQueue.enqueue(message)
      deliverBuf()
    }
  }

  @tailrec final def deliverBuf(): Unit = {
    if (isActive && totalDemand > 0)
      protoMessageQueue.dequeueOption match {
        case Some((el, q)) ⇒
          protoMessageQueue = q
          onNext(el)
          deliverBuf()
        case None ⇒
      }
  }
}
