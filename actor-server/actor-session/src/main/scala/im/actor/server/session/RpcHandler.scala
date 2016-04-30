package im.actor.server.session

import akka.actor._
import akka.event.Logging.MDC
import akka.http.scaladsl.util.FastFuture
import akka.pattern.pipe
import akka.stream.actor._
import cats.data.Xor
import com.github.kxbmap.configs.Bytes
import im.actor.api.rpc.codecs.RequestCodec
import im.actor.api.rpc.{ ClientData, Error, Ok, RpcError, RpcInternalError, RpcOk, RpcResult }
import im.actor.concurrent._
import im.actor.server.api.rpc.RpcApiExtension
import im.actor.util.cache.CacheHelpers._
import scodec.{ Attempt, DecodeResult }

import scala.annotation.tailrec
import scala.collection.immutable
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

private[session] object RpcHandler {
  private[session] val MaxCacheSize = 100L
  private[session] val RequestTimeOut = 30 seconds

  private[session] def props(authId: Long, sessionId: Long, config: RpcConfig) = Props(classOf[RpcHandler], authId, sessionId, config)

  private case class CachedResponse(messageId: Long, rsp: RpcResult, clientData: ClientData)

  private case class Response(messageId: Long, rsp: RpcResult, clientData: ClientData)

  private case class ResponseFailure(messageId: Long, request: im.actor.api.rpc.Request, failure: Throwable, clientData: ClientData)

  private case class Ack(messageId: Long)

  type AckOrResult = Either[Long, RpcResult]

  private val DefaultErrorDelay = 5

  object RpcErrors {
    val InternalError = RpcInternalError(canTryAgain = true, tryAgainDelay = DefaultErrorDelay)
    val RequestNotSupported = RpcError(400, "REQUEST_NOT_SUPPORTED", "Request is not supported.", canTryAgain = true, data = None)
  }

}

private[session] case class RpcConfig(maxCachedResults: Long, maxCachedResultSize: Bytes, ackDelay: FiniteDuration)

private[session] class RpcHandler(authId: Long, sessionId: Long, config: RpcConfig) extends ActorSubscriber with ActorPublisher[(Option[RpcResult], Long)] with ImActorLogging {

  import ActorPublisherMessage._
  import ActorSubscriberMessage._
  import RpcHandler._
  import SessionStreamMessage._

  import context._

  def receive = subscriber orElse publisher orElse {
    case unmatched ⇒
      log.error("Unmatched msg {}", unmatched)
  }

  // TODO: configurable
  private[this] val MaxRequestQueueSize = 10
  private[this] var requestQueue = Map.empty[Long, Cancellable]

  private[this] var protoMessageQueue = immutable.Queue.empty[(Option[RpcResult], Long)]

  // FIXME: invalidate on incoming ack
  private[this] val responseCache =
    createCache[java.lang.Long, Future[ResponseFailure Xor RpcResult]](config.maxCachedResults)

  def subscriber: Receive = {
    case OnNext(HandleRpcRequest(messageId, requestBytes, clientData)) ⇒
      recordClientData(clientData)

      Option(responseCache.getIfPresent(messageId)) match {
        case Some(rspFuture) ⇒
          rspFuture map (_ fold (identity, CachedResponse(messageId, _, clientData))) pipeTo self
        case None ⇒
          val scheduledAck = context.system.scheduler.scheduleOnce(config.ackDelay, self, Ack(messageId))
          requestQueue += (messageId → scheduledAck)
          assert(requestQueue.size <= MaxRequestQueueSize, s"queued too many: ${requestQueue.size}")

          val responseFuture =
            RequestCodec.decode(requestBytes) match {
              case Attempt.Successful(DecodeResult(request, _)) ⇒
                log.debug("Request messageId {}: {}, userId: {}", messageId, request, userIdOpt)

                val resultFuture = handleRequest(request, clientData) map Xor.right recover {
                  case e: Throwable ⇒ Xor.left(ResponseFailure(messageId, request, e, clientData))
                }
                responseCache.put(messageId, resultFuture)

                resultFuture.map(_ map (Response(messageId, _, clientData)) fold (identity, identity))
              case Attempt.Failure(err) ⇒
                log.warning("Failed to decode request: {}", err.messageWithContext)
                FastFuture.successful(Response(messageId, RpcErrors.RequestNotSupported, clientData))
            }

          responseFuture.pipeTo(self)
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
    case Response(messageId, rsp, clientData) ⇒
      log.debug("Response for messageId {}: {}", messageId, rsp)

      if (!canCache(rsp))
        responseCache.invalidate(messageId)

      removeFromQueue(messageId)
      enqueue(Some(rsp), messageId)
    case CachedResponse(messageId, rsp, clientData) ⇒
      log.debug("Response (cached) for messageId {}: {}", messageId, rsp)
      enqueue(Some(rsp), messageId)
    case ResponseFailure(messageId, request, failure, clientData) ⇒
      markFailure {
        log.error(failure, "Failed to process request messageId: {}: {}", messageId, request)
        responseCache.invalidate(messageId)
        removeFromQueue(messageId)
        enqueue(Some(RpcErrors.InternalError), messageId)
      }
    case Ack(messageId) ⇒ enqueueAck(messageId)
    case Request(_)     ⇒ deliverBuf()
    case Cancel         ⇒ context.stop(self)
  }

  private def removeFromQueue(messageId: Long): Unit = {
    requestQueue.get(messageId) foreach (_.cancel())
    requestQueue -= messageId
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

  private[this] var userIdOpt: Option[Int] = None

  private def recordClientData(clientData: ClientData): Unit = {
    if (userIdOpt.isEmpty && clientData.optUserId.isDefined)
      this.userIdOpt = clientData.optUserId
  }

  private def handleRequest(request: im.actor.api.rpc.Request, clientData: ClientData) = {
    val resultFuture =
      RpcApiExtension(context.system).chain.lift.apply(request.body) match {
        case Some(handler) ⇒ handler(clientData)
        case None          ⇒ FastFuture.successful(Error(RpcErrors.RequestNotSupported))
      }

    resultFuture.withTimeout(20.seconds).map {
      case Ok(result: RpcOk) ⇒ result
      case Error(error)      ⇒ error
    }
  }

  override def mdc(currentMessage: Any): MDC = {
    val base: MDC = Map("authId" → authId, "sessionId" → sessionId)
    userIdOpt.fold(base)(userId ⇒ base + ("userId" → userId))
  }
}
