package im.actor.server.api.service

import akka.actor._
import akka.stream.actor.ActorPublisher
import im.actor.server.api.mtproto.codecs.protocol.MessageBoxCodec
import im.actor.server.api.mtproto.protocol._
import im.actor.server.api.mtproto.transport._
import im.actor.server.persist
import scala.util.{ Success, Failure, Random }
import scalaz._

object AuthorizationActor {
  def props() = Props(new AuthorizationActor)
}

class AuthorizationActor extends Actor with ActorLogging with ActorPublisher[MTTransport] {
  import akka.stream.actor.ActorPublisherMessage._
  import context.dispatcher

  private var authId: Long = 0L

  def receive = {
    case p: MTPackage =>
      val replyTo = sender()
      MessageBoxCodec.decode(p.messageBytes) match {
        case \/-((_, mb)) => handleMessageBox(p.authId, p.sessionId, mb, replyTo)
        case -\/(e) => replyTo ! ProtoPackage(Drop(0, 0, e.message))
      }
    case _: Request =>
//      if (totalDemand > 0) onNext(ProtoPackage(Drop(0, 0, s"totalDemand: $totalDemand")))
  }

  private def handleMessageBox(pAuthId: Long, pSessionId: Long, mb: MessageBox, replyTo: ActorRef) = {
    @inline
    def sendPackage(messageId: Long, message: ProtoMessage) = {
      val mbBytes = MessageBoxCodec.encodeValid(MessageBox(messageId, message))
      replyTo ! ProtoPackage(MTPackage(authId, pSessionId, mbBytes))
    }

    @inline
    def sendDrop(msg: String) = replyTo ! ProtoPackage(Drop(mb.messageId, 0, msg))

    if (pAuthId == 0L) {
      if (pSessionId == 0L) sendDrop("sessionId must be equal to zero")
      else if (!mb.body.isInstanceOf[RequestAuthId]) sendDrop("non RequestAuthId message")
      else {
        @inline
        def sendResponseAuthId() = sendPackage(mb.messageId, ResponseAuthId(authId))

        if (authId == 0L) {
          authId = new Random().nextLong()
          persist.AuthId.create(authId, None).onComplete {
            case Success(_) => sendResponseAuthId()
            case Failure(e) => sendDrop(e.getMessage)
          }
        } else sendResponseAuthId()
      }
    } else {
      if (authId == 0L) authId = pAuthId
      if (authId == pAuthId) ??? // sessionRegion.tell(SessionProtocol.Envelope(p.authId, p.sessionId, mb)
      else sendDrop("authId cannot be changed more than once")
    }
  }
}
