package im.actor.server.session

import java.util.concurrent.TimeUnit

import scala.annotation.tailrec
import scala.collection.immutable
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import akka.actor.{ ActorRef, ActorLogging, Cancellable, Props }
import akka.stream.actor._
import com.typesafe.config.Config

import im.actor.server.mtproto.codecs.protocol.MessageBoxCodec
import im.actor.server.mtproto.protocol._
import im.actor.server.mtproto.transport.MTPackage

import scala.util.control.NoStackTrace

sealed trait ReSenderMessage

object ReSenderMessage {
  case class NewClient(client: ActorRef) extends ReSenderMessage
}

case class ReSenderConfig(ackTimeout: FiniteDuration, maxResendSize: Long, maxBufferSize: Long)
object ReSenderConfig {
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
  extends ActorSubscriber with ActorPublisher[MTPackage] with ActorLogging with MessageIdHelper {
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
    case NewClient(actorRef) ⇒
      log.debug("New client, sending all scheduled for resend")
      resendBuffer foreach {
        case (messageId, (msg, scheduledResend)) ⇒
          scheduledResend.cancel()
          enqueueProtoMessageWithResend(messageId, msg)
      }
    case unmatched ⇒
      log.error("Unmatched msg {}", unmatched)
  }

  private[this] var resendBufferSize = 0L
  private[this] var resendBuffer = immutable.SortedMap.empty[Long, (ProtoMessage with ResendableProtoMessage, Cancellable)]

  // Subscriber-related

  def subscriber: Receive = {
    case OnNext(msg: MessageAck with IncomingProtoMessage) ⇒
      // TODO: #perf possibly can be optimized
      msg.messageIds foreach { messageId ⇒
        resendBuffer.get(messageId) foreach {
          case (message, scheduledResend) ⇒
            resendBufferSize -= message.bodySize
            log.debug("Received Ack {}, cancelling resend", messageId)
            scheduledResend.cancel()
        }
      }
      resendBuffer --= msg.messageIds
    case OnNext(msg: ProtoMessage with OutgoingProtoMessage with ResendableProtoMessage) ⇒ enqueueProtoMessageWithResend(msg)
    case OnNext(msg: ProtoMessage with OutgoingProtoMessage)                             ⇒ enqueueProtoMessage(msg)
    case OnNext(RequestResend(messageId)) ⇒
      resendBuffer.get(messageId) map {
        case (msg, scheduledResend) ⇒
          // should be already completed because RequestResend is sent by client only after receiving Unsent notification
          scheduledResend.cancel()
          enqueueProtoMessageWithResend(messageId, msg)
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
        case (message, _) ⇒
          log.debug("Resending {}: {}", messageId, message)

          resendBufferSize -= message.bodySize

          message match {
            case rspBox @ RpcResponseBox(requestMessageId, bodyBytes) ⇒
              if (message.bodySize <= MaxResendSize) {
                enqueueProtoMessageWithResend(messageId, rspBox)
              } else {
                scheduleResend(messageId, rspBox)
                enqueueProtoMessage(nextMessageId(), UnsentResponse(messageId, requestMessageId, message.bodySize))
              }
            case ub @ UpdateBox(bodyBytes) ⇒
              if (message.bodySize <= MaxResendSize) {
                enqueueProtoMessageWithResend(messageId, ub)
              } else {
                scheduleResend(messageId, ub)
                enqueueProtoMessage(nextMessageId(), UnsentMessage(messageId, message.bodySize))
              }
            case msg ⇒
              enqueueProtoMessageWithResend(messageId, message)
          }
      }
  }

  // Publisher-related

  override val requestStrategy = WatermarkRequestStrategy(10) // TODO: configurable

  // Publisher-related

  private[this] var packageQueue = immutable.Queue.empty[MTPackage]

  def publisher: Receive = {
    case Request(_) ⇒
      deliverBuf()
    case Cancel ⇒
      context.stop(self)
  }

  private def enqueueProtoMessageWithResend(message: ProtoMessage with ResendableProtoMessage): Unit = {
    enqueueProtoMessageWithResend(nextMessageId(), message)
  }

  private def enqueueProtoMessageWithResend(messageId: Long, message: ProtoMessage with ResendableProtoMessage): Unit = {
    scheduleResend(messageId, message)
    enqueueProtoMessage(messageId, message)
  }

  private def scheduleResend(messageId: Long, message: ProtoMessage with ResendableProtoMessage): Unit = {
    log.debug("Scheduling resend of messageId: {}, timeout: {}", messageId, AckTimeout)

    resendBufferSize += message.bodySize

    if (resendBufferSize <= MaxBufferSize) {
      val scheduledResend = context.system.scheduler.scheduleOnce(AckTimeout, self, ScheduledResend(messageId))
      resendBuffer = resendBuffer.updated(messageId, (message, scheduledResend))
    } else {
      val msg = "Completing stream due to maximum buffer size reached"
      log.warning(msg)
      onErrorThenStop(new Exception(msg) with NoStackTrace)
    }
  }

  private def enqueueProtoMessage(message: ProtoMessage): (MTPackage, Long) =
    enqueueProtoMessage(nextMessageId(), message)

  private def enqueueProtoMessage(messageId: Long, message: ProtoMessage): (MTPackage, Long) = {
    val pkg = packProtoMessage(messageId, message)

    if (packageQueue.isEmpty && totalDemand > 0) {
      onNext(pkg)
    } else {
      packageQueue = packageQueue.enqueue(pkg)
      deliverBuf()
    }

    (pkg, messageId)
  }

  @tailrec final def deliverBuf(): Unit = {
    if (isActive && totalDemand > 0)
      packageQueue.dequeueOption match {
        case Some((el, q)) ⇒
          packageQueue = q
          onNext(el)
          deliverBuf()
        case None ⇒
      }
  }

  private def packProtoMessage(messageId: Long, message: ProtoMessage): MTPackage = {
    val mb = boxProtoMessage(messageId, message)
    packMessageBox(mb)
  }

  private def packMessageBox(mb: MessageBox): MTPackage = {
    val bytes = MessageBoxCodec.encode(mb).require
    MTPackage(authId, sessionId, bytes)
  }

  private def boxProtoMessage(messageId: Long, message: ProtoMessage): MessageBox = {
    MessageBox(messageId, message)
  }

  private def cleanup(): Unit = {
    resendBuffer foreach {
      case (_, (_, scheduledResend)) ⇒
        scheduledResend.cancel()
    }
  }
}
