package im.actor.server.session

import java.util.concurrent.TimeUnit

import akka.actor.{ ActorLogging, ActorRef, Cancellable, Props }
import akka.stream.actor._
import com.typesafe.config.Config
import im.actor.api.rpc.{ RpcOk, UpdateBox, RpcResult ⇒ ApiRpcResult }
import im.actor.api.rpc.codecs.UpdateBoxCodec
import im.actor.api.rpc.sequence._
import im.actor.server.api.rpc.RpcResultCodec
import im.actor.server.mtproto.protocol._
import scodec.bits.BitVector

import scala.annotation.tailrec
import scala.collection.{ immutable, mutable }
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

private[session] case class ReSenderConfig(ackTimeout: FiniteDuration, maxResendSize: Long, maxBufferSize: Long, maxPushBufferSize: Long)

private[session] object ReSenderConfig {
  def fromConfig(config: Config): ReSenderConfig = {
    ReSenderConfig(
      ackTimeout = config.getDuration("ack-timeout", TimeUnit.SECONDS).seconds,
      maxResendSize = config.getBytes("max-resend-size"),
      maxBufferSize = config.getBytes("max-buffer-size"),
      maxPushBufferSize = config.getBytes("max-push-buffer-size")
    )
  }
}

private[session] object ReSender {

  private case class ScheduledResend(messageId: Long, item: ResendableItem)

  private sealed trait ResendableItem {
    val bitsSize: Long
    val size = bitsSize / 8
    val priority: Priority
  }

  private object RpcItem {
    def apply(result: ApiRpcResult, requestMessageId: Long): RpcItem =
      RpcItem(RpcResultCodec.encode(result).require, requestMessageId)
  }
  private final case class RpcItem(body: BitVector, requestMessageId: Long) extends ResendableItem {
    override lazy val bitsSize = body.size
    override val priority = Priority.RPC
  }
  private object PushItem {
    def apply(ub: UpdateBox, reduceKeyOpt: Option[String]): PushItem = {
      val priority = ub match {
        case _: SeqUpdate | _: FatSeqUpdate ⇒ Priority.SeqPush
        case _: WeakUpdate                  ⇒ Priority.WeakPush
      }
      PushItem(UpdateBoxCodec.encode(ub).require, reduceKeyOpt, priority)
    }
  }
  private final case class PushItem(body: BitVector, reduceKeyOpt: Option[String], priority: Priority) extends ResendableItem {
    override lazy val bitsSize = body.size
  }
  private final case class NewSessionItem(newSession: NewSession) extends ResendableItem {
    override val bitsSize = 0L
    override val priority = Priority.NewSession
  }

  sealed trait Priority {
    val id: Int
  }
  object Priority {
    object NewSession extends Priority {
      override val id = 2
    }
    object Ack extends Priority {
      override val id = 1
    }
    object RPC extends Priority {
      override val id = 0
    }
    object SeqPush extends Priority {
      override val id = -1
    }
    object WeakPush extends Priority {
      override val id = -2
    }
  }

  private case object BufferOverflow

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

  // TODO: configurable
  private val AckTimeout = config.ackTimeout
  private val MaxBufferSize = config.maxBufferSize
  private val MaxResendSize = config.maxResendSize

  def receive = resendingToNewClients

  def resendingToNewClients: Receive = subscriber.orElse(publisher).orElse {
    case NewClient(_) ⇒
      log.debug("New client, sending all scheduled for resend")

      this.mbQueue.clear()
      this.resendBufferSize = 0
      this.resendPushBufferSize = 0

      this.newSessionBuffer foreach {
        case (messageId, ni, scheduled) ⇒
          scheduled.cancel()
          enqueueNewSession(ni)
      }

      this.responseBuffer foreach {
        case (messageId, (pi, scheduled)) ⇒
          scheduled.cancel()
          enqueueRpc(pi, nextMessageId())
      }

      this.pushBuffer foreach {
        case (messageId, (pi, scheduled)) ⇒
          scheduled.cancel()
          enqueuePush(pi, nextMessageId())
      }
  }

  private[this] var resendBufferSize = 0L
  private[this] var resendPushBufferSize = 0L
  private[this] var updateOptimizations = Set.empty[ApiUpdateOptimization.Value]

  private[this] var newSessionBuffer: Option[(Long, NewSessionItem, Cancellable)] = None
  private[this] var responseBuffer = immutable.SortedMap.empty[Long, (RpcItem, Cancellable)]
  private[this] var pushBuffer = immutable.SortedMap.empty[Long, (PushItem, Cancellable)]

  // Provides mapping from reduceKey to the last message with the reduceKey
  private[this] var pushReduceMap = immutable.Map.empty[String, Long]

  // Provides mapping from request messageId to a responseMessageId
  // to prevent response duplicates when client re-requests with same messageId
  type RequestMessageId = Long
  type ResponseMessageId = Long
  private[this] var rpcMap = immutable.Map.empty[RequestMessageId, ResponseMessageId]

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
            decreaseBufferSize(item)

            scheduledResend.cancel()

            item match {
              case PushItem(_, reduceKeyOpt, _) ⇒
                reduceKeyOpt foreach { reduceKey ⇒
                  if (pushReduceMap.get(reduceKey).contains(messageId))
                    pushReduceMap -= reduceKey
                }
                pushBuffer -= messageId
              case _: RpcItem ⇒
                responseBuffer -= messageId
                rpcMap -= messageId
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
              enqueuePush(pi, nextMessageId())
            case ri: RpcItem ⇒
              enqueueRpc(ri, nextMessageId())
            case ni: NewSessionItem ⇒
              enqueueNewSession(ni)
          }
      }
    case OnNext(RpcResult(rsp, requestMessageId)) ⇒
      val item = RpcItem(rsp, requestMessageId)

      this.rpcMap get requestMessageId match {
        // we are trying to deliver this response already,
        // so we cancel previous scheduled resend as client already requested a resend by doubling RPC request
        case Some(responseMessageId) ⇒
          responseBuffer.get(responseMessageId) map (_._2.cancel()) match {
            case Some(false) ⇒
            case _           ⇒ enqueueRpc(item, responseMessageId)
          }
        // it's a new rpc response
        case None ⇒
          val responseMessageId = nextMessageId()
          this.rpcMap += (requestMessageId → responseMessageId)
          enqueueRpc(item, responseMessageId)
      }
    case OnNext(p @ Push(_, reduceKey))       ⇒ enqueuePush(PushItem(p.ub, reduceKey), nextMessageId())
    case OnNext(SetUpdateOptimizations(opts)) ⇒ this.updateOptimizations = opts
    case OnComplete ⇒
      log.debug("Stopping due to stream completion")
      // TODO: cleanup scheduled resends
      context.stop(self)
    case OnError(cause) ⇒
      log.error(cause, "Stopping due to stream error")
      // TODO: cleanup scheduled resends
      context.stop(self)
    case ScheduledResend(messageId, item) ⇒
      if (getResendableItem(messageId).isDefined) {
        log.debug("Scheduled resend for messageId: {}, item: {}, resending", messageId, item)

        decreaseBufferSize(item)

        item match {
          case ni: NewSessionItem ⇒ enqueueNewSession(ni)
          case pi: PushItem ⇒
            if (pi.size > MaxResendSize)
              enqueueUnsentPush(pi, messageId)
            else
              enqueuePush(pi, messageId)
          case ri: RpcItem ⇒
            if (ri.size > MaxResendSize)
              enqueueUnsentRpc(ri, messageId)
            else
              enqueueRpc(ri, messageId)
        }
      } else log.debug("ScheduledResend for messageId: {}, item: {}, ignoring (absent in buffer)", messageId, item)
    case BufferOverflow ⇒
      if (this.resendBufferSize > config.maxBufferSize) {
        log.warning("Buffer overflow, stopping session")
        this.onCompleteThenStop()
      }
  }

  private def increaseBufferSize(item: ResendableItem): Unit = {
    this.resendBufferSize += item.size

    item match {
      case p: PushItem ⇒
        if (this.resendPushBufferSize > config.maxPushBufferSize)
          clearPushBuffer()
        else
          this.resendPushBufferSize += item.size
      case _ ⇒
    }
  }

  private def decreaseBufferSize(item: ResendableItem): Unit = {
    this.resendBufferSize -= item.size

    item match {
      case _: PushItem ⇒ this.resendPushBufferSize -= item.size
      case _           ⇒
    }
  }

  private def clearPushBuffer(): Unit = {
    log.debug("Push buffer exceeded, clearing and sending SeqUpdateTooLong")

    pushBuffer foreach {
      case (messageId, (pi: PushItem, resend)) ⇒
        pushBuffer -= messageId
        decreaseBufferSize(pi)
        resend.cancel()
      case _ ⇒
    }

    enqueueSeqUpdateTooLong()
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

    // FIXME: increase resendBufferSize by real Unsent

    if (resendBufferSize <= MaxBufferSize) {
      val delay = calcScheduleDelay()
      val scheduled = context.system.scheduler.scheduleOnce(delay, self, ScheduledResend(messageId, item))

      item match {
        case pi @ PushItem(_, reduceKeyOpt, _) ⇒
          reduceKeyOpt foreach { reduceKey ⇒
            for {
              msgId ← pushReduceMap.get(reduceKey)
              (ritem, resend) ← pushBuffer.get(msgId)
            } yield {
              this.pushBuffer -= msgId
              decreaseBufferSize(ritem)
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

    increaseBufferSize(item)
  }

  private def enqueueAcks(messageIds: Seq[Long]): Unit =
    enqueue(MessageBox(nextMessageId(), MessageAck(messageIds.toVector)), Priority.Ack)

  private def enqueueNewSession(item: NewSessionItem): Unit = {
    val messageId = nextMessageId()
    scheduleResend(item, messageId)
    enqueue(MessageBox(messageId, item.newSession), Priority.NewSession)
  }

  private def enqueueSeqUpdateTooLong(): Unit =
    enqueue(MessageBox(nextMessageId(), ProtoPush(UpdateBoxCodec.encode(SeqUpdateTooLong).require)), Priority.SeqPush)

  private def enqueueRpc(item: RpcItem, messageId: Long): Unit = {
    scheduleResend(item, messageId)
    val mb = MessageBox(messageId, ProtoRpcResponse(item.requestMessageId, item.body))
    enqueue(mb, Priority.RPC)
  }

  private def enqueueUnsentRpc(item: RpcItem, unsentMessageId: Long): Unit = {
    scheduleResend(item, unsentMessageId)
    val mb = MessageBox(nextMessageId(), UnsentResponse(unsentMessageId, item.requestMessageId, item.size.toInt))
    enqueue(mb, Priority.RPC)
  }

  private def enqueuePush(item: PushItem, messageId: Long): Unit = {
    scheduleResend(item, messageId)
    val mb = MessageBox(messageId, ProtoPush(item.body))
    enqueue(mb, item.priority)
  }

  private def enqueueUnsentPush(item: PushItem, unsentMessageId: Long): Unit = {
    scheduleResend(item, unsentMessageId)
    val mb = MessageBox(nextMessageId(), UnsentMessage(unsentMessageId, item.size.toInt))
    enqueue(mb, item.priority)
  }

  private def enqueue(mb: MessageBox, priority: Priority): Unit = {
    log.debug("Queue size: {}, bufferSize: {}, pushBufferSize: {}", mbQueue.size, resendBufferSize, resendPushBufferSize)

    if (isActive && totalDemand > 0 && mbQueue.isEmpty) {
      onNext(mb)
    } else {
      this.mbQueue.enqueue(mb → priority)
      deliverBuf()
    }
  }

  private def bufferOverflow(): Unit = {
    self ! BufferOverflow
  }

  private def pushBufferSize = responseBuffer.size + pushBuffer.size + newSessionBuffer.map(_ ⇒ 1).getOrElse(0)

  override def postStop(): Unit = {
    super.postStop()
    log.debug("Clearing resend buffers ({} items)", pushBufferSize)
    responseBuffer.values foreach (_._2.cancel())
    pushBuffer.values foreach (_._2.cancel())
    newSessionBuffer foreach (_._3.cancel())
  }
}
