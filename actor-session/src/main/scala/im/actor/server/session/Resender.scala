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

sealed trait ReSenderMessage

object ReSenderMessage {
  case class NewClient(client: ActorRef) extends ReSenderMessage
}

case class ReSenderConfig(ackTimeout: FiniteDuration, maxSize: Long)
object ReSenderConfig {
  def fromConfig(config: Config): ReSenderConfig = {
    ReSenderConfig(
      ackTimeout = config.getDuration("ack-timeout", TimeUnit.SECONDS).seconds,
      maxSize = config.getBytes("max-size")
    )
  }
}

private[session] object ReSender {
  def props(authId: Long, sessionId: Long)(implicit config: ReSenderConfig) =
    Props(classOf[ReSender], authId, sessionId, config)
}

private[session] class ReSender(authId: Long, sessionId: Long)(implicit config: ReSenderConfig)
  extends ActorSubscriber with ActorPublisher[MTPackage] with ActorLogging with MessageIdHelper {
  import ActorPublisherMessage._
  import ActorSubscriberMessage._

  import ReSenderMessage._

  // TODO: configurable
  private val AckTimeout = config.ackTimeout
  private val MaxResendSize = config.maxSize

  implicit val ec: ExecutionContext = context.dispatcher

  private case class ScheduledResend(messageId: Long)

  def receive = waitingForFirstClient

  def waitingForFirstClient: Receive = subscriber.orElse(publisher).orElse {
    case NewClient(_) ⇒
      context.become(resendingToNewClients)
    case unmatched ⇒
      log.error("Unmatched msg {}", unmatched)
  }

  def resendingToNewClients: Receive = subscriber.orElse(publisher).orElse {
    case NewClient(actorRef) ⇒
      resendBuffer foreach {
        case (messageId, (msg, scheduledResend)) ⇒
          scheduledResend.cancel()
          enqueueProtoMessageWithResend(messageId, msg)
      }
    case unmatched ⇒
      log.error("Unmatched msg {}", unmatched)
  }

  private[this] var resendBuffer = immutable.Map.empty[Long, (ProtoMessage, Cancellable)]

  // Subscriber-related

  def subscriber: Receive = {
    case OnNext(msg: MessageAck with IncomingProtoMessage) ⇒
      resendBuffer --= msg.messageIds
    case OnNext(msg: NewSession)                             ⇒ enqueueProtoMessage(msg)
    case OnNext(msg: MessageAck with OutgoingProtoMessage)   ⇒ enqueueProtoMessage(msg)
    case OnNext(msg: ProtoMessage with OutgoingProtoMessage) ⇒ enqueueProtoMessageWithResend(nextMessageId(), msg)
    case OnNext(RequestResend(messageId)) ⇒
      resendBuffer.get(messageId) map {
        case (msg, scheduledResend) ⇒
          // should be already completed because RequestResend is sent by client only after receiving Unsent notification
          scheduledResend.cancel()
          enqueueProtoMessageWithResend(messageId, msg)
      }
    case ScheduledResend(messageId) ⇒
      log.debug("Scheduled resend for messageId: {}", messageId)
      resendBuffer.get(messageId) map {
        case (msg, _) ⇒
          msg match {
            case RpcResponseBox(requestMessageId, bodyBytes) ⇒
              val bodySize = bodyBytes.bytes.size

              if (bodySize <= MaxResendSize) {
                enqueueProtoMessageWithResend(messageId, msg)
              } else {
                scheduleResend(messageId, msg)
                enqueueProtoMessage(nextMessageId(), UnsentResponse(messageId, requestMessageId, bodySize))
              }
            case UpdateBox(bodyBytes) ⇒
              val bodySize = bodyBytes.bytes.size

              if (bodySize <= MaxResendSize) {
                enqueueProtoMessageWithResend(messageId, msg)
              } else {
                scheduleResend(messageId, msg)
                enqueueProtoMessage(nextMessageId(), UnsentMessage(messageId, bodySize))
              }
            case wrong ⇒ // should never happen because we don't resend other message types
              log.error("Scheduled wrong message resend: {}", wrong)
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

  private def enqueueProtoMessageWithResend(messageId: Long, message: ProtoMessage): Unit = {
    scheduleResend(messageId, message)
    enqueueProtoMessage(messageId, message)
  }

  private def scheduleResend(messageId: Long, message: ProtoMessage): Unit = {
    log.debug("Scheduling resend of messageId: {}, timeout: {}", messageId, AckTimeout)
    val scheduledResend = context.system.scheduler.scheduleOnce(AckTimeout, self, ScheduledResend(messageId))
    resendBuffer = resendBuffer.updated(messageId, (message, scheduledResend))
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
}
