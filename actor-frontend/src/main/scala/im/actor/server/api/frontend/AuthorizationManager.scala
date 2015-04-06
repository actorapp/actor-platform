package im.actor.server.api.frontend

import scala.annotation.tailrec
import scala.concurrent.Future
import scala.util.{ Failure, Success }

import akka.actor._
import akka.stream.actor.ActorPublisher
import slick.driver.PostgresDriver.api.Database

import im.actor.server.api.util.rand
import im.actor.server.mtproto.codecs.protocol.MessageBoxCodec
import im.actor.server.mtproto.protocol._
import im.actor.server.mtproto.transport._
import im.actor.server.persist

object AuthorizationManager {

  @SerialVersionUID(1L)
  case class FrontendPackage(p: MTPackage)

  @SerialVersionUID(1L)
  case class SessionPackage(p: MTProto)

  def props(db: Database) = Props(classOf[AuthorizationManager], db)
}

class AuthorizationManager(db: Database) extends Actor with ActorLogging with ActorPublisher[MTTransport] {

  import akka.stream.actor.ActorPublisherMessage._
  import context.dispatcher

  import AuthorizationManager._

  private[this] var authId: Long = 0L
  private[this] var buf = Vector.empty[MTProto]

  def receive = {
    case FrontendPackage(p) =>
      val replyTo = sender()
      MessageBoxCodec.decode(p.messageBytes).toEither match {
        case Right(res) => handleMessageBox(p.authId, p.sessionId, res.value, replyTo)
        case Left(e) => replyTo ! ProtoPackage(Drop(0, 0, e.message))
      }
    /*case SessionPackage(p) =>
      if (buf.isEmpty && totalDemand > 0)
        onNext(ProtoPackage(p))
      else {
        buf :+= p
        deliverBuf()
      }*/
    case Request(_) =>
      deliverBuf()
    case Cancel =>
      context.stop(self)
  }

  private def handleMessageBox(pAuthId: Long, pSessionId: Long, mb: MessageBox, replyTo: ActorRef) = {
    @inline
    def sendPackage(messageId: Long, message: ProtoMessage) = {
      val mbBytes = MessageBoxCodec.encode(MessageBox(messageId, message)).require
      replyTo ! ProtoPackage(MTPackage(authId, pSessionId, mbBytes))
    }

    @inline
    def sendDrop(msg: String) = replyTo ! ProtoPackage(Drop(mb.messageId, 0, msg))

    if (pAuthId == 0L) {
      if (pSessionId == 0L) sendDrop("sessionId must be equal to zero")
      else mb.body match {
        case RequestAuthId() =>
          val f =
            if (authId == 0L) {
              authId = rand.nextLong()
              db.run(persist.AuthId.create(authId, None))
            } else Future.successful(())

          f.onComplete {
            case Success(_) => sendPackage(mb.messageId, ResponseAuthId(authId))
            case Failure(e) => sendDrop(e.getMessage)
          }
        case _ => sendDrop("not a RequestAuthId message")
      }
    } else {
      log.error("AuthorizationManager can handle packages with authId: 0 only")
    }
  }

  @tailrec
  private def deliverBuf(): Unit =
    if (totalDemand > 0) {
      if (totalDemand <= Int.MaxValue) {
        val (use, keep) = buf.splitAt(totalDemand.toInt)
        buf = keep
        use.foreach { p => onNext(ProtoPackage(p)) }
      } else {
        val (use, keep) = buf.splitAt(Int.MaxValue)
        buf = keep
        use.foreach { p => onNext(ProtoPackage(p)) }
        deliverBuf()
      }
    }
}
