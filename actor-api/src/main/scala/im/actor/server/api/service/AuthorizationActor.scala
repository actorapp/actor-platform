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
  import akka.stream.actor.ActorPublisherMessage._

  private var authId: Long = 0L

  def receive = {
    case p: MTPackage =>
      val replyTo = sender()
      MessageBoxCodec.decode(p.messageBytes) match {
        case \/-((_, mb)) => handleMessageBox(p, mb, replyTo)
        case -\/(e) => replyTo ! ProtoPackage(Drop(0, 0, e.message))
      }
    case _: Request =>
      println("Request !!!!!!!!!!!!!!!!!!!")
//      if (totalDemand > 0) onNext(ProtoPackage(Drop(0, 0, s"totalDemand: $totalDemand")))
  }

  private def handleMessageBox(p: MTPackage, mb: MessageBox, replyTo: ActorRef) = {
    @inline
    def sendPackage(messageId: Long, message: ProtoMessage) = {
      val mbBytes = MessageBoxCodec.encodeValid(MessageBox(messageId, message))
      replyTo ! ProtoPackage(MTPackage(authId, p.sessionId, mbBytes))
    }

    @inline
    def sendDrop(msg: String) = replyTo ! ProtoPackage(Drop(mb.messageId, 0, msg))

    if (p.authId == 0L) {
      if (p.sessionId == 0L) sendDrop("sessionId must be equal to zero")
      else if (!mb.body.isInstanceOf[RequestAuthId]) sendDrop("non RequestAuthId message")
      else {
        if (authId == 0L) {
          authId = new Random().nextLong()
          // TODO: Insert auth id
        }
        sendPackage(mb.messageId, ResponseAuthId(authId))
      }
    } else {
      if (authId == 0L) authId = p.authId
      if (authId == p.authId) ??? // sessionRegion.tell(SessionProtocol.Envelope(p.authId, p.sessionId, mb)
      else sendDrop("authId cannot be changed more than once")
    }
    log.debug(s"p: $p, mb: $mb")
  }
}
