package im.actor.server.session

import scala.annotation.tailrec
import scala.collection.immutable

import akka.actor._
import akka.stream.actor._

import im.actor.api.rpc.ClientData
import im.actor.server.mtproto.protocol._

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
      log.debug("MessageBox: {} clientData: {}", mb, clientData)

      // TODO: tail-recursive function for container unpacking
      mb.body match {
        case Container(bodies) ⇒
          val ackMessage = HandleOutgoingAck(
            bodies
              .view
              .filter {
                case MessageBox(_, _: MessageAck)      ⇒ false
                case MessageBox(_, _: ProtoRpcRequest) ⇒ false
                case _                                 ⇒ true
              }
              .map(_.messageId) :+ mb.messageId
          )
          val handleMessages = bodies.map(HandleMessageBox(_, clientData)).toList
          publishMessages(ackMessage :: handleMessages)
        case _ ⇒
          val handleMessage = HandleMessageBox(mb, clientData)

          mb.body match {
            case _: MessageAck      ⇒ publishMessage(handleMessage)
            case _: ProtoRpcRequest ⇒ publishMessages(List(handleMessage))
            case _                  ⇒ publishMessages(List(HandleOutgoingAck(Seq(mb.messageId)), handleMessage))
          }
      }
    case command: SubscribeCommand ⇒
      publishMessage(HandleSubscribe(command))
    case Request(_) ⇒
      deliverBuf()
    case Cancel ⇒
      context.stop(self)
    case unmatched ⇒
      log.debug("Unmatched {}", unmatched)
  }

  private def publishMessage(message: SessionStreamMessage): Unit = {
    log.debug("Publish message {}", message)

    if (messageQueue.isEmpty && totalDemand > 0)
      onNext(message)
    else {
      messageQueue = messageQueue.enqueue(message)
      deliverBuf()
    }
  }

  private def publishMessages(messages: immutable.Iterable[SessionStreamMessage]): Unit = {
    messages foreach { message ⇒
      log.debug("Publish message {}", message)
    }
    messageQueue = messageQueue.enqueue(messages)
    deliverBuf()
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
