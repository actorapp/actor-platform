package im.actor.server.api.frontend

import scala.annotation.tailrec
import scala.collection.immutable

import akka.actor._
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{ Cancel, Request }
import com.google.protobuf.ByteString
import scodec.bits.BitVector

import im.actor.server.mtproto.codecs.protocol.MessageBoxCodec
import im.actor.server.mtproto.protocol.{ MessageBox, SessionLost }
import im.actor.server.mtproto.{ transport ⇒ T }
import im.actor.server.session.{ HandleMessageBox, SessionEnvelope, SessionRegion }

private[frontend] object SessionClient {

  @SerialVersionUID(1L)
  case class SendToSession(p: T.MTPackage)

  def props(sessionRegion: SessionRegion) = Props(classOf[SessionClient], sessionRegion)
}

private[frontend] class SessionClient(sessionRegion: SessionRegion) extends Actor with ActorLogging with ActorPublisher[T.MTProto] {

  import SessionClient.SendToSession

  private[this] var packageQueue = immutable.Queue.empty[T.MTProto]

  def receive: Receive = watchForSession

  def watchForSession: Receive = publisher orElse {
    case SendToSession(T.MTPackage(authId, sessionId, messageBytes)) ⇒
      sessionRegion.ref ! SessionEnvelope(authId, sessionId).withHandleMessageBox(HandleMessageBox(ByteString.copyFrom(messageBytes.toByteBuffer)))
    case p: T.MTPackage ⇒
      context.watch(sender())
      enqueuePackage(p)
      context.become(working(p.authId, p.sessionId))
  }

  def working(authId: Long, sessionId: Long): Receive = publisher orElse {
    case SendToSession(T.MTPackage(authId, sessionId, messageBytes)) ⇒
      sessionRegion.ref ! SessionEnvelope(authId, sessionId).withHandleMessageBox(HandleMessageBox(ByteString.copyFrom(messageBytes.toByteBuffer)))
    case p @ T.MTPackage(authId, sessionId, mbBits: BitVector) ⇒
      enqueuePackage(p)
    case Terminated(sessionRef) ⇒
      val p = T.MTPackage(authId, sessionId, MessageBoxCodec.encode(MessageBox(Long.MaxValue, SessionLost)).require)
      enqueuePackage(p)
      context.become(watchForSession.orElse(publisher))
  }

  def publisher: Receive = {
    case Request(_) ⇒
      deliverBuf()
    case Cancel ⇒
      context.stop(self)
  }

  private def enqueuePackage(p: T.MTPackage): Unit = {
    if (packageQueue.isEmpty && totalDemand > 0) {
      onNext(p)
    } else {
      packageQueue = packageQueue.enqueue(p)
      deliverBuf()
    }
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
