package im.actor.server.session

import java.util.concurrent.TimeUnit

import akka.actor.{ ActorLogging, ActorRef, Cancellable, Props }
import akka.stream.actor._
import com.typesafe.config.Config
import im.actor.server.mtproto.protocol._

import scala.annotation.tailrec
import scala.collection.immutable
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.control.NoStackTrace

private[session] sealed trait ReSenderMessage

private[session] object ReSenderMessage {

  case class NewClient(client: ActorRef) extends ReSenderMessage

  case class IncomingAck(messageIds: Seq[Long]) extends ReSenderMessage

  case class IncomingRequestResend(messageId: Long) extends ReSenderMessage

  case class OutgoingMessage(msg: ProtoMessage, reduceKey: Option[String]) extends ReSenderMessage

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

  private case class ScheduledResend(messageId: Long)

  def props(authId: Long, sessionId: Long)(implicit config: ReSenderConfig) =
    Props(classOf[ReSender], authId, sessionId, config)
}

private[session] class ReSender(authId: Long, sessionId: Long)(implicit config: ReSenderConfig)
  extends ActorSubscriber with ActorPublisher[MessageBox] with ActorLogging with MessageIdHelper {

  import ActorPublisherMessage._
  import ActorSubscriberMessage._
  import ReSender._
  import ReSenderMessage._

  // TODO: configurable
  private val AckTimeout = config.ackTimeout
  private val MaxBufferSize = config.maxBufferSize
  private val MaxResendSize = config.maxResendSize

  implicit val ec: ExecutionContext = context.dispatcher

  def receive = waitingForFirstClient

  def waitingForFirstClient: Receive = subscriber.orElse(publisher).orElse {
    case NewClient(_) ⇒
      context.become(resendingToNewClients)
    case unmatched ⇒
      log.error("Unmatched msg {}", unmatched)
  }

  def resendingToNewClients: Receive = subscriber.orElse(publisher).orElse {
    case NewClient(_) ⇒
      log.debug("New client, sending all scheduled for resend")
      resendBuffer foreach {
        case (messageId, (msg, reduceKey, scheduledResend)) ⇒
          scheduledResend.cancel()
          enqueueProtoMessageWithResend(messageId, msg, reduceKey)
      }
    case unmatched ⇒
      log.error("Unmatched msg {}", unmatched)
  }

  private[this] var resendBufferSize = 0L
  private[this] var resendBuffer = immutable.SortedMap.empty[Long, (ProtoMessage with ResendableProtoMessage, Option[String], Cancellable)]

  // Provides mapping from reduceKey to the last message with the reduceKey
  private[this] var reduceMap = immutable.Map.empty[String, Long]

  // Used to prevent scheduling multiple updates at the same millisecond and result out of order
  private[this] var lastScheduledResend = System.currentTimeMillis - 1

  // Subscriber-related

  def subscriber: Receive = {
    case OnNext(IncomingAck(messageIds)) ⇒
      // TODO: #perf possibly can be optimized
      messageIds foreach { messageId ⇒
        resendBuffer.get(messageId) foreach {
          case (message, reduceKeyOpt, scheduledResend) ⇒
            resendBufferSize -= message.bodySize
            log.debug("Received Ack {}, cancelling resend", messageId)
            scheduledResend.cancel()

            reduceKeyOpt foreach (cleanReduceKey(_, messageId))
        }
      }
      resendBuffer --= messageIds
    case OnNext(OutgoingMessage(msg: ProtoMessage with OutgoingProtoMessage with ResendableProtoMessage, reduceKey: Option[String])) ⇒
      enqueueProtoMessageWithResend(msg, reduceKey)
    case OnNext(OutgoingMessage(msg: ProtoMessage with OutgoingProtoMessage, _)) ⇒ enqueueProtoMessage(msg)
    case OnNext(IncomingRequestResend(messageId)) ⇒
      resendBuffer.get(messageId) foreach {
        case (msg, reduceKey, scheduledResend) ⇒
          // should be already completed because RequestResend is sent by client only after receiving Unsent notification
          scheduledResend.cancel()
          enqueueProtoMessageWithResend(messageId, msg, None)
      }
    case OnComplete ⇒
      log.debug("Stopping due to stream completion")
      cleanup()
      context.stop(self)
    case OnError(cause) ⇒
      log.error(cause, "Stopping due to stream error")
      cleanup()
      context.stop(self)
    case ScheduledResend(messageId) ⇒
      log.debug("Scheduled resend for messageId: {}", messageId)
      resendBuffer.get(messageId) map {
        case (message, reduceKey, _) ⇒
          log.debug("Resending {}: {}, reduceKey: {}", messageId, message, reduceKey)

          resendBufferSize -= message.bodySize

          message match {
            case rspBox @ ProtoRpcResponse(requestMessageId, bodyBytes) ⇒
              if (message.bodySize <= MaxResendSize) {
                enqueueProtoMessageWithResend(messageId, rspBox, reduceKey)
              } else {
                scheduleResend(messageId, rspBox, reduceKey)
                enqueueProtoMessage(nextMessageId(), UnsentResponse(messageId, requestMessageId, message.bodySize))
              }
            case ub @ ProtoPush(bodyBytes) ⇒
              if (message.bodySize <= MaxResendSize) {
                enqueueProtoMessageWithResend(messageId, ub, reduceKey)
              } else {
                scheduleResend(messageId, ub, reduceKey)
                enqueueProtoMessage(nextMessageId(), UnsentMessage(messageId, message.bodySize))
              }
            case msg ⇒
              enqueueProtoMessageWithResend(messageId, message, reduceKey)
          }
      }
  }

  // Publisher-related

  override val requestStrategy = WatermarkRequestStrategy(10) // TODO: configurable

  // Publisher-related

  private[this] var mbQueue = immutable.Queue.empty[MessageBox]

  def publisher: Receive = {
    case Request(_) ⇒
      deliverBuf()
    case Cancel ⇒
      context.stop(self)
  }

  private def enqueueProtoMessageWithResend(message: ProtoMessage with ResendableProtoMessage, reduceKeyOpt: Option[String]): Unit =
    enqueueProtoMessageWithResend(nextMessageId(), message, reduceKeyOpt)

  private def enqueueProtoMessageWithResend(messageId: Long, message: ProtoMessage with ResendableProtoMessage, reduceKeyOpt: Option[String]): Unit = {
    scheduleResend(messageId, message, reduceKeyOpt)
    enqueueProtoMessage(messageId, message)
  }

  private def scheduleResend(messageId: Long, message: ProtoMessage with ResendableProtoMessage, reduceKeyOpt: Option[String]): Unit = {
    log.debug("Scheduling resend of messageId: {}, timeout: {}", messageId, AckTimeout)

    resendBufferSize += message.bodySize

    if (resendBufferSize <= MaxBufferSize) {
      reduceKeyOpt foreach { reduceKey ⇒
        for {
          msgId ← reduceMap.get(reduceKey)
          (msg, _, scheduledResend) ← resendBuffer.get(msgId)
        } yield {
          resendBuffer -= msgId
          resendBufferSize -= msg.bodySize
          scheduledResend.cancel()
        }

        reduceMap += (reduceKey → messageId)
      }

      val currentTime = System.currentTimeMillis()

      val scheduleDelay =
        if (currentTime > this.lastScheduledResend) {
          this.lastScheduledResend = currentTime
          AckTimeout
        } else {
          val delta = this.lastScheduledResend - currentTime + 1
          this.lastScheduledResend = currentTime + delta
          AckTimeout + delta.milli
        }

      val scheduledResend = context.system.scheduler.scheduleOnce(scheduleDelay, self, ScheduledResend(messageId))
      resendBuffer = resendBuffer.updated(messageId, (message, reduceKeyOpt, scheduledResend))
    } else {
      val msg = "Completing stream due to maximum buffer size reached"
      log.warning(msg)
      onErrorThenStop(new Exception(msg) with NoStackTrace)
    }
  }

  private def enqueueProtoMessage(message: ProtoMessage): (MessageBox, Long) =
    enqueueProtoMessage(nextMessageId(), message)

  private def enqueueProtoMessage(messageId: Long, message: ProtoMessage): (MessageBox, Long) = {
    val mb = MessageBox(messageId, message)

    if (mbQueue.isEmpty && totalDemand > 0) {
      onNext(mb)
    } else {
      mbQueue = mbQueue.enqueue(mb)
      deliverBuf()
    }

    (mb, messageId)
  }

  @tailrec final def deliverBuf(): Unit = {
    if (isActive && totalDemand > 0)
      mbQueue.dequeueOption match {
        case Some((el, q)) ⇒
          mbQueue = q
          onNext(el)
          deliverBuf()
        case None ⇒
      }
  }

  /**
   * Removes mapping from reduceMap if messageId equals to the one stored in the mapping
   *
   * @param reduceKey
   * @param messageId
   */
  private def cleanReduceKey(reduceKey: String, messageId: Long): Unit = {
    if (reduceMap.get(reduceKey).contains(messageId))
      reduceMap -= reduceKey
  }

  private def cleanup(): Unit = {
    resendBuffer foreach {
      case (_, (_, _, scheduledResend)) ⇒
        scheduledResend.cancel()
    }
  }
}
