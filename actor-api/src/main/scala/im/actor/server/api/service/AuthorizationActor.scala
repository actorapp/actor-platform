package im.actor.server.api.service

import akka.actor._
import akka.stream.actor.ActorPublisher
import im.actor.server.api.mtproto.codecs.protocol.MessageBoxCodec
import im.actor.server.api.mtproto.protocol._
import im.actor.server.api.mtproto.transport._
import scala.util.Random
import scalaz._

object AuthorizationActor {
  def props() = Props(new AuthorizationActor)
}

class AuthorizationActor extends Actor with ActorLogging with ActorPublisher[MTTransport] {
  import context.dispatcher
  import scala.concurrent.duration._
  import akka.stream.actor.ActorPublisherMessage._

  case object Wow

  context.system.scheduler.schedule(1.second, 1.second, self, Wow)

  def receive = {
    case MTPackage(authId, sessionId, m) =>
      val replyTo = sender()
      MessageBoxCodec.decode(m) match {
        case \/-((_, mb)) => handleMessageBox(authId, sessionId, mb, replyTo)
        case -\/(e) => replyTo ! ProtoPackage(Drop(0, 0, e.message))
      }
    case _: Request =>
      println("Request !!!!!!!!!!!!!!!!!!!")
    case Wow =>
      println(s"totalDemand: $totalDemand")
      if (totalDemand > 0) onNext(ProtoPackage(Drop(0, 0, s"totalDemand: $totalDemand")))
  }

  private def handleMessageBox(authId: Long, sessionId: Long, mb: MessageBox, replyTo: ActorRef) = {
    @inline
    def sendPackage(messageId: Long, message: ProtoMessage) = {
      val mbBytes = MessageBoxCodec.encodeValid(MessageBox(messageId, message))
      replyTo ! ProtoPackage(MTPackage(authId, sessionId, mbBytes))
    }

    @inline
    def sendDrop(msg: String) = replyTo ! ProtoPackage(Drop(mb.messageId, 0, msg))

    if (authId == 0L) {
      if (sessionId == 0L) sendDrop("sessionId must be equal to zero")
      else if (!mb.body.isInstanceOf[RequestAuthId]) sendDrop("non RequestAuthId message")
      else {
        val newAuthId = new Random().nextLong()
        // TOD: Insert auth id
        sendPackage(mb.messageId, ResponseAuthId(newAuthId))
      }
    } else println("TODO")
    log.debug(s"authId: $authId, sessionId: $sessionId, mb: $mb")
  }
}
