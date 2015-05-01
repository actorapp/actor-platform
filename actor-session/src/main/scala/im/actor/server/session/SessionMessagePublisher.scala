package im.actor.server.session

import scala.annotation.tailrec
import scala.collection.immutable

import akka.actor._
import akka.stream.actor._

import im.actor.api.rpc.ClientData
import im.actor.server.mtproto.protocol.{ Container, MessageBox }
import im.actor.server.session.SessionMessage.SubscribeCommand

private[session] object SessionMessagePublisher {
  def props() = Props[SessionMessagePublisher]
}

private[session] class SessionMessagePublisher extends ActorPublisher[SessionStreamMessage] with ActorLogging {

  import ActorPublisherMessage._

  import SessionStreamMessage._

  // TODO: MaxQueueSize
  private[this] var messageQueue = immutable.Queue.empty[SessionStreamMessage]

  def receive = {
    case (mb: MessageBox, clientData: ClientData) ⇒
      log.info("MessageBox: {} clientData: {}", mb, clientData)

      mb.body match {
        case Container(bodies) ⇒
          val messages = bodies.map(HandleMessageBox(_, clientData))
          messageQueue = messageQueue.enqueue(messages.toList)
          deliverBuf()
        case _ ⇒
          if (messageQueue.isEmpty && totalDemand > 0)
            onNext(HandleMessageBox(mb, clientData))
          else {
            messageQueue = messageQueue.enqueue(HandleMessageBox(mb, clientData))
            deliverBuf()
          }
      }
    case command: SubscribeCommand ⇒
      val elem = HandleSubscribe(command)

      if (messageQueue.isEmpty && totalDemand > 0) {
        onNext(elem)
      } else {
        messageQueue = messageQueue.enqueue(elem)
        deliverBuf()
      }
    case Request(_) ⇒
      deliverBuf()
    case Cancel ⇒
      context.stop(self)
    case unmatched ⇒
      log.debug("Unmatched {}", unmatched)
  }

  @tailrec final def deliverBuf(): Unit =
    if (isActive && totalDemand > 0)
      messageQueue.dequeueOption match {
        case Some((el, queue)) ⇒
          messageQueue = queue
          onNext(el)
          deliverBuf()
        case None ⇒
      }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.error(reason, "Exception thrown, message: {}", message)
  }
}
