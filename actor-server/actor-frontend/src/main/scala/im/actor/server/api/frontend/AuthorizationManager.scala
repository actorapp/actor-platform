package im.actor.server.api.frontend

import im.actor.server.db.DbExtension
import im.actor.util.misc.IdUtils

import scala.annotation.tailrec
import scala.concurrent.Future
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.util.{ Failure, Success }

import akka.actor._
import akka.stream.actor.ActorPublisher
import slick.driver.PostgresDriver.api.Database

import im.actor.server.mtproto.codecs.protocol.MessageBoxCodec
import im.actor.server.mtproto.protocol._
import im.actor.server.mtproto.transport._
import im.actor.server.persist

object AuthorizationManager {

  @SerialVersionUID(1L)
  case class FrontendPackage(p: MTPackage)

  @SerialVersionUID(1L)
  case class SessionPackage(p: MTProto)

  def props = Props(classOf[AuthorizationManager])
}

class AuthorizationManager extends Actor with ActorLogging with ActorPublisher[MTProto] {

  import akka.stream.actor.ActorPublisherMessage._
  import context.dispatcher

  import AuthorizationManager._

  private val db: Database = DbExtension(context.system).db

  private[this] var authId: Long = 0L
  private[this] var buf = Vector.empty[MTProto]

  def receive = {
    case FrontendPackage(p) ⇒
      MessageBoxCodec.decode(p.messageBytes).toEither match {
        case Right(res) ⇒ handleMessageBox(p.authId, p.sessionId, res.value)
        case Left(e) ⇒
          log.error("Failed to decode MessageBox: {}", e)
          enqueue(Drop(0, 0, "Failed to decode MessageBox"))
          onCompleteThenStop()

      }
    case Request(_) ⇒
      deliverBuf()
    case Cancel ⇒
      context.stop(self)
  }

  private def handleMessageBox(pAuthId: Long, pSessionId: Long, mb: MessageBox) = {
    @inline
    def sendPackage(messageId: Long, message: ProtoMessage) = {
      val mbBytes = MessageBoxCodec.encode(MessageBox(messageId, message)).require
      enqueue(MTPackage(authId, pSessionId, mbBytes))
    }

    @inline
    def sendDrop(msg: String) = {
      enqueue(Drop(mb.messageId, 0, msg))
      onCompleteThenStop()
    }

    if (pAuthId == 0L) {
      if (pSessionId != 0L) sendDrop("sessionId must be equal to zero")
      else mb.body match {
        case RequestAuthId ⇒
          val f =
            if (authId == 0L) {
              authId = IdUtils.nextAuthId(ThreadLocalRandom.current())
              db.run(persist.AuthId.create(authId, None, None))
            } else Future.successful(())

          f.onComplete {
            case Success(_) ⇒ sendPackage(mb.messageId, ResponseAuthId(authId))
            case Failure(e) ⇒ sendDrop(e.getMessage)
          }
        case _ ⇒ sendDrop("not a RequestAuthId message")
      }
    } else {
      log.error("AuthorizationManager can handle packages with authId: 0 only")
    }
  }

  private def enqueue(p: MTProto): Unit = {
    if (buf.isEmpty && totalDemand > 0) {
      onNext(p)
    } else {
      buf = buf :+ p
      deliverBuf()
    }
  }

  @tailrec
  private def deliverBuf(): Unit =
    if (totalDemand > 0) {
      if (totalDemand <= Int.MaxValue) {
        val (use, keep) = buf.splitAt(totalDemand.toInt)
        buf = keep
        use.foreach { p ⇒ onNext(p) }
      } else {
        val (use, keep) = buf.splitAt(Int.MaxValue)
        buf = keep
        use.foreach { p ⇒ onNext(p) }
        deliverBuf()
      }
    }
}
