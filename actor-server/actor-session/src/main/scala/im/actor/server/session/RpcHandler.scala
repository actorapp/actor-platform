package im.actor.server.session

import scala.annotation.tailrec
import scala.collection.immutable
import scala.concurrent.{ Promise, Future }
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.control.NoStackTrace
import scala.util.{ Try, Failure, Success }

import akka.actor._
import akka.pattern.pipe
import akka.stream.actor._
import scodec.bits._

import im.actor.api.rpc.RpcInternalError
import im.actor.server.api.rpc.{ RpcResultCodec, RpcApiService }
import im.actor.server.api.rpc.RpcApiService.RpcResponse
import im.actor.server.mtproto.protocol.{ ProtoMessage, RpcResponseBox }
import im.actor.util.cache.CacheHelpers._

private[session] object RpcHandler {
  private[session] val MaxCacheSize = 100L
  private[session] val RequestTimeOut = 30 seconds

  def props = Props(classOf[RpcHandler])

  private case class CachedResponse(rsp: RpcApiService.RpcResponse)
}

private[session] object RequestHandler {
  private[session] def props(promise: Promise[RpcApiService.RpcResponse], service: ActorSelection, request: RpcApiService.HandleRpcRequest) =
    Props(classOf[RequestHandler], promise, service, request)
}

private[session] class RequestHandler(
  promise: Promise[RpcApiService.RpcResponse],
  service: ActorSelection,
  request: RpcApiService.HandleRpcRequest
)
  extends Actor with ActorLogging {

  context.setReceiveTimeout(RpcHandler.RequestTimeOut)

  override def preStart(): Unit = {
    super.preStart()
    service ! request
  }

  def receive = {
    case rsp: RpcApiService.RpcResponse ⇒ complete(Success(rsp))
    case ReceiveTimeout ⇒
      log.error("Request timed out")
      val rsp = RpcResponse(request.messageId, RpcResultCodec.encode(RpcInternalError(true, 1)).require)
      complete(Success(rsp))
    case unexpected ⇒
      log.error("Unexpected message {}", unexpected)
      complete(Failure(new Exception("Got unexpected message") with NoStackTrace))
  }

  private def complete(result: Try[RpcApiService.RpcResponse]) = {
    promise.complete(result)
    context.stop(self)
  }
}

private[session] class RpcHandler extends ActorSubscriber with ActorPublisher[ProtoMessage] with ActorLogging {

  import ActorPublisherMessage._
  import ActorSubscriberMessage._

  import SessionStreamMessage._

  import RpcHandler._

  private implicit val ec = context.dispatcher

  private[this] val rpcApiService: ActorSelection = context.actorSelection("/user/rpcApiService")

  def receive = subscriber.orElse(publisher).orElse {
    case unmatched ⇒
      log.error("Unmatched msg {}", unmatched)
  }

  // TODO: configurable
  private[this] val MaxRequestQueueSize = 10
  private[this] var requestQueue = Map.empty[Long, BitVector]

  private[this] var protoMessageQueue = immutable.Queue.empty[ProtoMessage]
  private[this] val responseCache = createCache[java.lang.Long, Future[RpcApiService.RpcResponse]](MaxCacheSize)

  def subscriber: Receive = {
    case OnNext(HandleRpcRequest(messageId, requestBytes, clientData)) ⇒
      Option(responseCache.getIfPresent(messageId)) match {
        case Some(rspFuture) ⇒
          log.debug("Publishing cached RpcResponse for messageId: {}", messageId)
          rspFuture map (CachedResponse(_)) pipeTo self
        case None ⇒
          requestQueue += (messageId → requestBytes)
          assert(requestQueue.size <= MaxRequestQueueSize, s"queued too many: ${requestQueue.size}")

          log.debug("Making an rpc request for messageId: {}", messageId)

          val responsePromise = Promise[RpcApiService.RpcResponse]()
          context.actorOf(
            RequestHandler.props(
              responsePromise,
              rpcApiService,
              RpcApiService.HandleRpcRequest(messageId, requestBytes, clientData)
            ),
            s"handler-${messageId}"
          )

          responseCache.put(messageId, responsePromise.future)

          responsePromise.future pipeTo self
      }
    case OnComplete ⇒
      context.stop(self)
    case OnError(cause) ⇒
      log.error(cause, "Error in upstream")
  }

  override val requestStrategy = new MaxInFlightRequestStrategy(max = MaxRequestQueueSize) {
    override def inFlightInternally: Int = requestQueue.size
  }

  def publisher: Receive = {
    case RpcApiService.RpcResponse(messageId, responseBytes) ⇒
      log.debug("Received RpcResponse for messageId: {}, publishing", messageId)

      requestQueue -= messageId
      enqueueProtoMessage(RpcResponseBox(messageId, responseBytes))
    case CachedResponse(rsp) ⇒
      log.debug("Got cached RpcResponse for messageId: {}, publishing", rsp.messageId)

      enqueueProtoMessage(RpcResponseBox(rsp.messageId, rsp.responseBytes))
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
