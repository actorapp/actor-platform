package im.actor.server.session

import akka.actor._
import akka.stream.actor._

import im.actor.server.mtproto.protocol.{ ProtoMessage, RpcResponseBox }

import scala.annotation.tailrec
import scala.collection.immutable

object RpcResponseManager {
  case class Stop(cause: Throwable)

  def props() = Props[RpcResponsePublisher]
}

class RpcResponsePublisher extends ActorPublisher[RpcResponseBox] with ActorLogging {
  import ActorPublisherMessage._
  import RpcResponseManager._

  var responseQueue = immutable.Queue.empty[RpcResponseBox]

  def receive = {
    case rspBox: RpcResponseBox =>
      log.debug("RpcResponseBox {}", rspBox)
      if (responseQueue.isEmpty && totalDemand > 0) {
        onNext(rspBox)
      } else {
        responseQueue = responseQueue.enqueue(rspBox)
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

  @tailrec final def deliverBuf(): Unit =
    if (isActive && totalDemand > 0)
      responseQueue.dequeueOption match {
        case Some((el, queue)) =>
          responseQueue = queue
          onNext(el)
          deliverBuf()
        case None =>
      }
}
