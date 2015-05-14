package im.actor.server.api.frontend

import scala.annotation.tailrec
import scala.collection.immutable

import akka.actor.{ Actor, ActorLogging, Props }
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{ Cancel, Request }
import scodec.bits.BitVector

import im.actor.server.mtproto.{ transport ⇒ T }
import im.actor.server.session.{ SessionMessage, SessionRegion }

private[frontend] object SessionClient {
  @SerialVersionUID(1L)
  case class SendToSession(p: T.MTPackage)

  def props(sessionRegion: SessionRegion) = Props(classOf[SessionClient], sessionRegion)
}

private[frontend] class SessionClient(sessionRegion: SessionRegion) extends Actor with ActorLogging with ActorPublisher[T.MTProto] {
  import SessionClient.SendToSession

  private[this] var packageQueue = immutable.Queue.empty[T.MTProto]

  def receive = {
    case SendToSession(T.MTPackage(authId, sessionId, messageBytes)) ⇒
      sessionRegion.ref ! SessionMessage.envelope(authId, sessionId, SessionMessage.HandleMessageBox(messageBytes.toByteArray))
    case p @ T.MTPackage(authId, sessionId, mbBits: BitVector) ⇒
      if (packageQueue.isEmpty && totalDemand > 0) {
        onNext(p)
      } else {
        packageQueue = packageQueue.enqueue(p)
        deliverBuf()
      }
    case Request(_) ⇒
      deliverBuf()
    case Cancel ⇒
      context.stop(self)
  }

  @tailrec
  private def deliverBuf(): Unit = {
    if (isActive && totalDemand > 0)
      packageQueue.dequeueOption match {
        case Some((el, queue)) ⇒
          packageQueue = queue
          onNext(el)
          deliverBuf()
        case None ⇒
      }
  }
}
