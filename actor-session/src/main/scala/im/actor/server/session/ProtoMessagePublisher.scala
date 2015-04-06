package im.actor.server.session

import scala.annotation.tailrec
import scala.collection.immutable

import akka.actor._
import akka.stream.actor._

import im.actor.server.mtproto.protocol.ProtoMessage

private[session] object ProtoMessagePublisher {

  @SerialVersionUID(1L)
  case class Stop(cause: Throwable)

  def props() = Props[ProtoMessagePublisher]
}

private[session] class ProtoMessagePublisher extends ActorPublisher[ProtoMessage] with ActorLogging {

  import ActorPublisherMessage._

  import ProtoMessagePublisher._

  private[this] var queue = immutable.Queue.empty[ProtoMessage]

  def receive = {
    case protoMessage: ProtoMessage =>
      log.debug("ProtoMessage: {}", protoMessage)
      if (queue.isEmpty && totalDemand > 0) {
        onNext(protoMessage)
      } else {
        queue = queue.enqueue(protoMessage)
        deliverBuf()
      }
    case Stop(cause) =>
      log.error(cause, "Received Stop")
      onError(cause)
      context.stop(self)
    case Request(_) =>
      deliverBuf()
    case Cancel =>
      context.stop(self)
  }

  @tailrec final def deliverBuf(): Unit = {
    if (isActive && totalDemand > 0)
      queue.dequeueOption match {
        case Some((el, q)) =>
          queue = q
          onNext(el)
          deliverBuf()
        case None =>
      }
  }
}
