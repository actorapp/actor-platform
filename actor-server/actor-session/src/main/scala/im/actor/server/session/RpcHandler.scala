package im.actor.server.session

import akka.actor._
import akka.pattern.pipe
import akka.stream.actor._
import com.github.kxbmap.configs.Bytes
import im.actor.api.rpc.{ RpcError, RpcInternalError, RpcOk, RpcResult }
import im.actor.server.api.rpc.RpcApiService.RpcResponse
import im.actor.server.api.rpc.{ RpcApiExtension, RpcApiService }
import im.actor.util.cache.CacheHelpers._

import scala.annotation.tailrec
import scala.collection.immutable
import scala.concurrent.duration._
import scala.concurrent.{ Future, Promise }
import scala.language.postfixOps
import scala.util.control.NoStackTrace
import scala.util.{ Failure, Success, Try }

private[session] object RpcHandler {
  private[session] val MaxCacheSize = 100L
  private[session] val RequestTimeOut = 30 seconds

  def props(config: RpcConfig) = Props(classOf[RpcHandler], config)

  private case class CachedResponse(rsp: RpcApiService.RpcResponse)
  private case class Ack(messageId: Long)

  type AckOrResult = Either[Long, RpcResult]
}

private[session] case class RpcConfig(maxCachedResults: Long, maxCachedResultSize: Bytes, ackDelay: FiniteDuration)

private[session] object RequestHandler {
  private[session] def props(promise: Promise[RpcApiService.RpcResponse], service: ActorRef, request: RpcApiService.HandleRpcRequest) =
    Props(classOf[RequestHandler], promise, service, request)
}

private[session] class RequestHandler(
  promise: Promise[RpcApiService.RpcResponse],
  service: ActorRef,
  request: RpcApiService.HandleRpcRequest
) extends Actor with ActorLogging {

  context.setReceiveTimeout(RpcHandler.RequestTimeOut)

  override def preStart(): Unit = {
    super.preStart()
    service ! request
  }

  def receive = {
    case rsp: RpcApiService.RpcResponse ⇒ complete(Success(rsp))
    case ReceiveTimeout ⇒
      log.error("Request timed out")
      val rsp = RpcResponse(request.messageId, RpcInternalError(true, 1))
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

private[session] class RpcHandler(config: RpcConfig) extends ActorSubscriber with ActorPublisher[(Option[RpcResult], Long)] with ActorLogging {

  import ActorPublisherMessage._
  import ActorSubscriberMessage._
  import RpcHandler._
  import SessionStreamMessage._

  private implicit val ec = context.dispatcher

  private[this] val rpcApiService = RpcApiExtension(context.system).serviceRef

  def receive = subscriber orElse publisher orElse {
    case unmatched ⇒
      log.error("Unmatched msg {}", unmatched)
  }

  // TODO: configurable
  private[this] val MaxRequestQueueSize = 10
  private[this] var requestQueue = Map.empty[Long, Cancellable]

  private[this] var protoMessageQueue = immutable.Queue.empty[(Option[RpcResult], Long)]

  // FIXME: invalidate on incoming ack
  private[this] val responseCache = createCache[java.lang.Long, Future[RpcApiService.RpcResponse]](config.maxCachedResults)

  def subscriber: Receive = {
    case OnNext(HandleRpcRequest(messageId, requestBytes, clientData)) ⇒
      Option(responseCache.getIfPresent(messageId)) match {
        case Some(rspFuture) ⇒
          log.debug("Publishing cached RpcResponse for messageId: {}", messageId)
          rspFuture map CachedResponse pipeTo self
        case None ⇒
          val scheduledAck = context.system.scheduler.scheduleOnce(config.ackDelay, self, Ack(messageId))
          requestQueue += (messageId → scheduledAck)
          assert(requestQueue.size <= MaxRequestQueueSize, s"queued too many: ${requestQueue.size}")

          log.debug("Making an rpc request for messageId: {}", messageId)

          val responsePromise = Promise[RpcApiService.RpcResponse]()
          context.actorOf(
            RequestHandler.props(
              responsePromise,
              rpcApiService,
              RpcApiService.HandleRpcRequest(messageId, requestBytes, clientData)
            ),
            s"handler-$messageId"
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
    case RpcApiService.RpcResponse(messageId, result) ⇒
      log.debug("Received RpcResponse for messageId: {}, publishing", messageId)

      if (!canCache(result))
        responseCache.invalidate(messageId)

      requestQueue.get(messageId) foreach (_.cancel())
      requestQueue -= messageId
      enqueue(Some(result), messageId)
    case CachedResponse(rsp) ⇒
      log.debug("Got cached RpcResponse for messageId: {}, publishing", rsp.messageId)
      enqueue(Some(rsp.result), rsp.messageId)
    case Ack(messageId) ⇒ enqueueAck(messageId)
    case Request(_)     ⇒ deliverBuf()
    case Cancel         ⇒ context.stop(self)
  }

  private def canCache(result: RpcResult): Boolean = {
    val size = result match {
      case RpcOk(res)    ⇒ res.getSerializedSize
      case err: RpcError ⇒ err.data.map(_.getSerializedSize).getOrElse(0) + err.userMessage.length
      case _             ⇒ 0
    }

    size < config.maxCachedResultSize.value
  }

  private def enqueueAck(requestMessageId: Long): Unit = enqueue(None, requestMessageId)

  private def enqueue(res: Option[RpcResult], requestMessageId: Long): Unit = {
    val item = res → requestMessageId

    if (protoMessageQueue.isEmpty && totalDemand > 0) {
      onNext(item)
    } else {
      protoMessageQueue = protoMessageQueue.enqueue(item)
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
