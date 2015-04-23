package im.actor.server.api.frontend

import scala.annotation.tailrec

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{ Cancel, Request }
import scodec.bits.BitVector

import im.actor.server.mtproto.{ transport ⇒ T }
import im.actor.server.session.{ SessionRegion, SessionMessage }

private[frontend] object SessionClient {
  @SerialVersionUID(1L)
  case class SendToSession(p: T.MTPackage)

  def props(sessionRegion: SessionRegion) = Props(classOf[SessionClient], sessionRegion)
}

private[frontend] class SessionClient(sessionRegion: SessionRegion) extends Actor with ActorLogging with ActorPublisher[T.MTTransport] {
  import SessionClient.SendToSession

  private[this] var buf = Vector.empty[T.MTProto]

  def receive = {
    case SendToSession(T.MTPackage(authId, sessionId, messageBytes)) ⇒
      sessionRegion.ref ! SessionMessage.envelope(authId, sessionId, SessionMessage.HandleMessageBox(messageBytes.toByteArray))
    case p @ T.MTPackage(authId, sessionId, mbBits: BitVector) ⇒
      if (buf.isEmpty && totalDemand > 0) {
        onNext(T.ProtoPackage(p))
      } else {
        buf :+= p
        deliverBuf()
      }
    case Request(_) ⇒
      deliverBuf()
    case Cancel ⇒
      context.stop(self)
  }

  @tailrec
  private def deliverBuf(): Unit = {
    if (totalDemand > 0) {
      if (totalDemand <= Int.MaxValue) {
        val (use, keep) = buf.splitAt(totalDemand.toInt)
        buf = keep
        use.foreach { p ⇒ onNext(T.ProtoPackage(p)) }
      } else {
        val (use, keep) = buf.splitAt(Int.MaxValue)
        buf = keep
        use.foreach { p ⇒ onNext(T.ProtoPackage(p)) }
        deliverBuf()
      }
    }
  }
}
