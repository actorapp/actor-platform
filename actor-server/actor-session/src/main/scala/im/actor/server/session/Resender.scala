package im.actor.server.session

import java.util.concurrent.TimeUnit

import akka.actor.{ ActorLogging, ActorRef, Cancellable, Props }
import akka.stream.actor._
import com.typesafe.config.Config
import im.actor.api.rpc.{ RpcResult ⇒ ApiRpcResult, UpdateBox }
import im.actor.api.rpc.codecs.UpdateBoxCodec
import im.actor.api.rpc.sequence.{ FatSeqUpdate, WeakUpdate, SeqUpdate, ApiUpdateOptimization }
import im.actor.server.api.rpc.RpcResultCodec
import im.actor.server.mtproto.protocol._

import scala.annotation.tailrec
import scala.collection.{ mutable, immutable }
import scala.concurrent.duration._
import scala.util.control.NoStackTrace

private[session] sealed trait ReSenderMessage

private[session] object ReSenderMessage {

  final case class NewClient(client: ActorRef) extends ReSenderMessage

  final case class IncomingAck(messageIds: Seq[Long]) extends ReSenderMessage

  final case class IncomingRequestResend(messageId: Long) extends ReSenderMessage

  // final case class OutgoingMessage(msg: ProtoMessage) extends ReSenderMessage
  final case class OutgoingAck(messageIds: Seq[Long]) extends ReSenderMessage

  final case class Push(ub: UpdateBox, reduceKey: Option[String]) extends ReSenderMessage

  final case class RpcResult(rsp: ApiRpcResult, requestMessageId: Long) extends ReSenderMessage

  final case class SetUpdateOptimizations(updateOptimizations: Set[ApiUpdateOptimization.Value]) extends ReSenderMessage
}

private[session] case class ReSenderConfig(ackTimeout: FiniteDuration, maxResendSize: Long, maxBufferSize: Long)

private[session] object ReSenderConfig {
  def fromConfig(config: Config): ReSenderConfig = {
    ReSenderConfig(
      ackTimeout = config.getDuration("ack-timeout", TimeUnit.SECONDS).seconds,
      maxResendSize = config.getBytes("max-resend-size"),
      maxBufferSize = config.getBytes("max-buffer-size")
    )
  }
}

private[session] object ReSender {

  private case class ScheduledResend(messageId: Long, item: ResendableItem)

  private sealed trait ResendableItem {
    val size: Long
  }
  private final case class RpcItem(result: ApiRpcResult, requestMessageId: Long) extends ResendableItem {
    lazy val body = RpcResultCodec.encode(result).require
    override lazy val size = body.size
    val reduceKeyOpt = None
  }
  private final case class PushItem(ub: UpdateBox, reduceKeyOpt: Option[String]) extends ResendableItem {
    lazy val body = UpdateBoxCodec.encode(ub).require
    override lazy val size = body.size
  }
  private final case class NewSessionItem(newSession: NewSession) extends ResendableItem {
    override val size = 0L
  }

  def props(authId: Long, sessionId: Long, firstMessageId: Long)(implicit config: ReSenderConfig) =
    Props(classOf[ReSender], authId, sessionId, firstMessageId, config)
}

private[session] class ReSender(authId: Long, sessionId: Long, firstMessageId: Long)(implicit config: ReSenderConfig)
  extends ActorSubscriber with ActorPublisher[MessageBox] with ActorLogging with MessageIdHelper {

  import ActorPublisherMessage._
  import ActorSubscriberMessage._
  import ReSender._
  import ReSenderMessage._
  import context.dispatcher

  sealed trait Priority {
    val id: Int
  }
  object Priority {
    object NewSession extends Priority {
      override val id = -2
    }
    object Ack extends Priority {
      override val id = -1
    }
    object RPC extends Priority {
      override val id = 0
    }
    object SeqPush extends Priority {
      override val id = 1
    }
    object WeakPush extends Priority {
      override val id = 2
    }
  }

  // TODO: configurable
  private val AckTimeout = config.ackTimeout
  private val MaxBufferSize = config.maxBufferSize
  private val MaxResendSize = config.maxResendSize

  def receive = resendingToNewClients

  def resendingToNewClients: Receive = subscriber.orElse(publisher).orElse {
    case NewClient(_) ⇒
      log.debug("New client, sending all scheduled for resend")

      this.mbQueue.clear()

      this.newSessionBuffer foreach {
        case (messageId, ni, scheduled) ⇒
          scheduled.cancel()
          enqueueNewSession(ni)
      }

      this.responseBuffer foreach {
        case (messageId, (pi, scheduled)) ⇒
          scheduled.cancel()
          enqueueRpc(pi, None)
      }

      this.pushBuffer foreach {
        case (messageId, (pi, scheduled)) ⇒
          scheduled.cancel()
          enqueuePush(pi, None)
      }
  }

  private[this] var resendBufferSize = 0L
  private[this] var updateOptimizations = Set.empty[ApiUpdateOptimization.Value]

  private[this] var newSessionBuffer: Option[(Long, NewSessionItem, Cancellable)] = None
  private[this] var responseBuffer = immutable.SortedMap.empty[Long, (RpcItem, Cancellable)]
  private[this] var pushBuffer = immutable.SortedMap.empty[Long, (PushItem, Cancellable)]

  // Provides mapping from reduceKey to the last message with the reduceKey
  private[this] var pushReduceMap = immutable.Map.empty[String, Long]

  // Used to prevent scheduling multiple updates at the same millisecond and result out of order
  private[this] var lastScheduledResend = System.currentTimeMillis - 1

  override def preStart(): Unit = {
    super.preStart()
    enqueueNewSession(NewSessionItem(NewSession(sessionId, firstMessageId)))
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.error(reason, "An error occured while processing message: {}", message)
    super.preRestart(reason, message)
  }

  // Subscriber-related

  def subscriber: Receive = {
    case OnNext(IncomingAck(messageIds)) ⇒
      log.debug("Received Acks {}", messageIds)

      messageIds foreach { messageId ⇒
        getResendableItem(messageId) foreach {
          case (item, scheduledResend) ⇒
            if (item.size <= MaxResendSize)
              resendBufferSize -= item.size
            scheduledResend.cancel()

            item match {
              case PushItem(_, reduceKeyOpt) ⇒
                reduceKeyOpt foreach { reduceKey ⇒
                  if (pushReduceMap.get(reduceKey).contains(messageId))
                    pushReduceMap -= reduceKey
                }
                pushBuffer -= messageId
              case _: RpcItem ⇒
                responseBuffer -= messageId
              case item: NewSessionItem ⇒
                this.newSessionBuffer = None
            }
        }
      }
    case OnNext(OutgoingAck(messageIds)) ⇒
      enqueueAcks(messageIds)
    case OnNext(IncomingRequestResend(messageId)) ⇒
      getResendableItem(messageId) foreach {
        case (item, scheduled) ⇒
          scheduled.cancel()

          item match {
            case pi: PushItem ⇒
              enqueuePush(pi, None)
            case ri: RpcItem ⇒
              enqueueRpc(ri, None)
            case ni: NewSessionItem ⇒
              enqueueNewSession(ni)
          }
      }
    case OnNext(RpcResult(rsp, requestMessageId)) ⇒ enqueueRpc(RpcItem(rsp, requestMessageId), None)
    case OnNext(p @ Push(_, reduceKey))           ⇒ enqueuePush(PushItem(p.ub, reduceKey), None)
    case OnNext(SetUpdateOptimizations(opts))     ⇒ this.updateOptimizations = opts
    case OnComplete ⇒
      log.debug("Stopping due to stream completion")
      // TODO: cleanup scheduled resends
      context.stop(self)
    case OnError(cause) ⇒
      log.error(cause, "Stopping due to stream error")
      // TODO: cleanup scheduled resends
      context.stop(self)
    case ScheduledResend(messageId, item) ⇒
      log.debug("Scheduled resend for messageId: {}, item: {}", messageId, item)

      if (item.size <= MaxResendSize)
        resendBufferSize -= item.size

      item match {
        case ni: NewSessionItem ⇒ enqueueNewSession(ni)
        case pi: PushItem       ⇒ enqueuePush(pi, Some(messageId))
        case ri: RpcItem        ⇒ enqueueRpc(ri, Some(messageId))
      }
  }

  // Publisher-related

  override val requestStrategy = WatermarkRequestStrategy(100) // TODO: configurable

  // Publisher-related

  private[this] val mbQueue = mutable.PriorityQueue.empty[(MessageBox, Priority)](Ordering.by { case (mb, p) ⇒ (p.id, mb.messageId) })

  def publisher: Receive = {
    case Request(n) ⇒
      deliverBuf()
    case Cancel ⇒
      context.stop(self)
  }

  @tailrec final def deliverBuf(): Unit = {
    if (isActive && totalDemand > 0 && mbQueue.nonEmpty)
      mbQueue.dequeue() match {
        case (mb, _) ⇒
          onNext(mb)
          deliverBuf()
      }
  }

  override def unhandled(message: Any): Unit = {
    super.unhandled(message)
    log.error("Unhandled {}", message)
  }

  private def getResendableItem(messageId: Long): Option[(ResendableItem, Cancellable)] = {
    responseBuffer
      .get(messageId)
      .orElse(pushBuffer.get(messageId))
      .orElse {
        this.newSessionBuffer match {
          case Some((`messageId`, item, scheduled)) ⇒
            Some((item, scheduled))
          case _ ⇒ None
        }
      }
  }

  private def calcScheduleDelay(): FiniteDuration = {
    val currentTime = System.currentTimeMillis()

    if (currentTime > this.lastScheduledResend) {
      this.lastScheduledResend = currentTime
      AckTimeout
    } else {
      val delta = this.lastScheduledResend - currentTime + 1
      this.lastScheduledResend = currentTime + delta
      AckTimeout + delta.milli
    }
  }

  private def scheduleResend(item: ResendableItem, messageId: Long) = {
    log.debug("Scheduling resend of messageId: {}, timeout: {}", messageId, AckTimeout)

    if (item.size <= MaxResendSize)
      this.resendBufferSize += item.size

    // FIXME: increase resendBufferSize by real Unsent

    if (resendBufferSize <= MaxBufferSize) {
      val delay = calcScheduleDelay()
      val scheduled = context.system.scheduler.scheduleOnce(delay, self, ScheduledResend(messageId, item))

      item match {
        case pi @ PushItem(_, reduceKeyOpt) ⇒
          reduceKeyOpt foreach { reduceKey ⇒
            for {
              msgId ← pushReduceMap.get(reduceKey)
              (ritem, resend) ← pushBuffer.get(msgId)
            } yield {
              this.pushBuffer -= msgId
              if (ritem.size <= MaxResendSize)
                resendBufferSize -= ritem.size
              resend.cancel()
            }

            this.pushReduceMap += (reduceKey → messageId)
          }

          this.pushBuffer = this.pushBuffer.updated(messageId, (pi, scheduled))
        case ni: NewSessionItem ⇒
          this.newSessionBuffer = Some((messageId, ni, scheduled))
        case ri: RpcItem ⇒
          this.responseBuffer = this.responseBuffer.updated(messageId, (ri, scheduled))
      }
    } else bufferOverflow()
  }

  private def enqueueAcks(messageIds: Seq[Long]): Unit =
    enqueue(MessageBox(nextMessageId(), MessageAck(messageIds.toVector)), Priority.Ack)

  private def enqueueNewSession(item: NewSessionItem): Unit = {
    val messageId = nextMessageId()
    scheduleResend(item, messageId)
    enqueue(MessageBox(messageId, item.newSession), Priority.NewSession)
  }

  private def enqueueRpc(item: RpcItem, unsentMessageIdOpt: Option[Long], isNewClient: Boolean = false): Unit = {
    val messageId = unsentMessageIdOpt.getOrElse(nextMessageId())
    scheduleResend(item, messageId)

    val mb =
      unsentMessageIdOpt match {
        case Some(unsentMessageId) if item.size > MaxResendSize ⇒
          MessageBox(nextMessageId(), UnsentResponse(unsentMessageId, item.requestMessageId, item.size.toInt))
        case _ ⇒
          MessageBox(messageId, ProtoRpcResponse(item.requestMessageId, item.body))
      }

    enqueue(mb, Priority.RPC)
  }

  private def enqueuePush(item: PushItem, unsentMessageIdOpt: Option[Long]): Unit = {
    val messageId = nextMessageId()
    scheduleResend(item, messageId)

    val msg =
      unsentMessageIdOpt match {
        case Some(unsentMessageId) if item.size > MaxResendSize ⇒ UnsentMessage(unsentMessageId, item.size.toInt)
        case _ ⇒ ProtoPush(item.body)
      }

    val priority = item.ub match {
      case _: SeqUpdate | _: FatSeqUpdate ⇒ Priority.SeqPush
      case _: WeakUpdate                  ⇒ Priority.WeakPush
    }

    enqueue(MessageBox(messageId, msg), priority)
  }

  private def enqueue(mb: MessageBox, priority: Priority): Unit = {
    if (isActive && totalDemand > 0 && mbQueue.isEmpty) {
      onNext(mb)
    } else {
      this.mbQueue.enqueue(mb → priority)
      deliverBuf()
    }
  }

  private def bufferOverflow(): Unit = {
    val msg = "Completing stream due to maximum buffer size reached"
    log.warning(msg)
    onError(new RuntimeException(msg) with NoStackTrace)
  }
}
