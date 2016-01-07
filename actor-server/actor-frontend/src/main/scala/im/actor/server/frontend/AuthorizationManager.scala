package im.actor.server.frontend

import java.nio.ByteBuffer
import java.security.{ SecureRandom, MessageDigest }

import better.files.File
import com.typesafe.config.Config
import im.actor.server.db.DbExtension
import im.actor.util.misc.IdUtils
import scodec.bits.BitVector

import scala.annotation.tailrec
import scala.collection.JavaConversions._
import scala.concurrent.Future
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.util.{ Try, Failure, Success }

import akka.actor._
import akka.stream.actor.ActorPublisher
import slick.driver.PostgresDriver.api.Database

import im.actor.server.mtproto.codecs.protocol.MessageBoxCodec
import im.actor.server.mtproto.protocol._
import im.actor.server.mtproto.transport._
import im.actor.server.persist

object ServerKey {
  def loadFromConfig(config: Config): Try[ServerKey] =
    for {
      publicPath ← Try(config.getString("public"))
      privatePath ← Try(config.getString("private"))
      public ← Try(File(publicPath).byteArray)
      privat ← Try(File(privatePath).byteArray)
    } yield new ServerKey(public, privat)

  def loadKeysFromConfig(config: Config): Try[Seq[ServerKey]] =
    Try {
      config.getConfigList("modules.security.server-keys").toIndexedSeq map (loadFromConfig(_).get)
    }
}

final class ServerKey(val public: Array[Byte], val privat: Array[Byte]) {
  lazy val hash: Array[Byte] = {
    val md = MessageDigest.getInstance("SHA-256")
    md.digest(public)
  }

  lazy val id: Long = {
    val bb = ByteBuffer.allocate(java.lang.Long.BYTES)
    bb.put(hash.take(4))
    bb.flip()
    bb.getLong
  }
}

object AuthorizationManager {

  @SerialVersionUID(1L)
  case class FrontendPackage(p: MTPackage)

  @SerialVersionUID(1L)
  case class SessionPackage(p: MTProto)

  def props(serverKeys: Seq[ServerKey]) = Props(classOf[AuthorizationManager], serverKeys)

  val NonceBytes = 32
}

final class AuthorizationManager(serverKeys: Seq[ServerKey]) extends Actor with ActorLogging with ActorPublisher[MTProto] {

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

  private def handleMessageBox(pAuthId: Long, pSessionId: Long, mb: MessageBox): Unit = {
    @inline
    def sendPackage(messageId: Long, message: ProtoMessage) = {
      val mbBytes = MessageBoxCodec.encode(MessageBox(messageId, message)).require
      enqueue(MTPackage(authId, pSessionId, mbBytes))
    }

    def withValidKey(keyId: Long)(f: ServerKey ⇒ Any): Unit =
      serverKeys find (_.id == keyId) match {
        case Some(serverKey) ⇒ f(serverKey)
        case None            ⇒ sendDrop("Wrong key id")
      }

    @inline
    def sendDrop(msg: String) = {
      enqueue(Drop(mb.messageId, 0, msg))
      onCompleteThenStop()
    }

    def handleRequestAuthId(): Unit = {
      val f =
        if (authId == 0L) {
          authId = IdUtils.nextAuthId(ThreadLocalRandom.current())
          db.run(persist.AuthIdRepo.create(authId, None, None))
        } else Future.successful(())

      f.onComplete {
        case Success(_) ⇒ sendPackage(mb.messageId, ResponseAuthId(authId))
        case Failure(e) ⇒ sendDrop(e.getMessage)
      }
    }

    def handleRequestStartAuth(randomId: Long): Unit = {
      val nonce = new Array[Byte](NonceBytes)
      new SecureRandom().nextBytes(nonce)
      val rsp = ResponseStartAuth(randomId, serverKeys.toVector map (_.id), BitVector(nonce))
      sendPackage(mb.messageId, rsp)
    }

    def handleRequestGetServerKey(keyId: Long): Unit = {
      withValidKey(keyId) { serverKey ⇒
        val rsp = ResponseGetServerKey(keyId, BitVector(serverKey.public))
        sendPackage(mb.messageId, rsp)
      }
    }
    /*
    @inline
    def handleRequestDH(randomId: Long, keyId: Long, clientNonce: BitVector, clientKey: BitVector): Unit = {
      withValidKey(keyId) { serverKey =>

        val rsp = ResponseDoDH(randomId, verify, verifySign)
        sendPackage(mb.messageId, rsp)
      }
    }*/

    if (pAuthId == 0L) {
      if (pSessionId != 0L) sendDrop("sessionId must be equal to zero")
      else mb.body match {
        case RequestAuthId              ⇒ handleRequestAuthId()
        case RequestStartAuth(randomId) ⇒ handleRequestStartAuth(randomId)
        case RequestGetServerKey(keyId) ⇒ handleRequestGetServerKey(keyId)
        // case RequestDH(randomId, keyId, clientNonce, clientKey) => handleRequestDH(randomId, keyId, clientNonce, clientKey)
        case _                          ⇒ sendDrop("not a RequestAuthId message")
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
