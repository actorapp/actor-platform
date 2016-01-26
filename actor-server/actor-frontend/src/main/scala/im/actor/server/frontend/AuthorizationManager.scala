package im.actor.server.frontend

import java.security.{ MessageDigest, SecureRandom }

import akka.actor._
import akka.pattern.pipe
import akka.stream.actor.ActorPublisher
import better.files.File
import com.google.common.primitives.Longs
import com.typesafe.config.Config
import im.actor.crypto.{ Cryptos, Curve25519 }
import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.db.DbExtension
import im.actor.server.mtproto.codecs.protocol.MessageBoxCodec
import im.actor.server.mtproto.protocol._
import im.actor.server.mtproto.transport._
import im.actor.server.persist
import im.actor.server.persist.{ AuthIdRepo, MasterKeyRepo }
import im.actor.util.misc.IdUtils
import im.actor.util.ThreadLocalSecureRandom
import scodec.bits.BitVector

import scala.annotation.tailrec
import scala.collection.JavaConversions._
import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }

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

  lazy val id: Long = Longs.fromByteArray(hash.take(java.lang.Long.BYTES))
}

object AuthorizationManager {

  @SerialVersionUID(1L)
  final case class FrontendPackage(p: MTPackage)

  @SerialVersionUID(1L)
  final case class SessionPackage(p: MTProto)

  @SerialVersionUID(1L)
  private case class SendPackage(sessionId: Long, messageId: Long, message: ProtoMessage)

  @SerialVersionUID(1L)
  private case class SendDrop(messageId: Long, message: String)

  def props(serverKeys: Seq[ServerKey], sessionClient: ActorRef) = Props(classOf[AuthorizationManager], serverKeys, sessionClient)

  val NonceBytes = 32
  val PRFBytes = 256
  val SignRandomBytes = 64
}

final class AuthorizationManager(serverKeys: Seq[ServerKey], sessionClient: ActorRef) extends Actor with ActorLogging with ActorPublisher[MTProto] {

  import AuthorizationManager._
  import akka.stream.actor.ActorPublisherMessage._
  import context.dispatcher

  private val db: Database = DbExtension(context.system).db

  private[this] var authId: Long = 0L
  private[this] var buf = Vector.empty[MTProto]
  private[this] var sessions = Map.empty[Long, BitVector]

  def receive = {
    case FrontendPackage(p) ⇒
      MessageBoxCodec.decode(p.messageBytes).toEither match {
        case Right(res) ⇒ handleMessageBox(p.authId, p.sessionId, res.value)
        case Left(e) ⇒
          log.error("Failed to decode MessageBox: {}", e)
          enqueue(Drop(0, 0, "Failed to decode MessageBox"))
          onCompleteThenStop()

      }
    case SendPackage(sessionId: Long, messageId, message) ⇒ sendPackage(sessionId, messageId, message)
    case SendDrop(messageId, message) ⇒
      enqueue(Drop(messageId, 0, message))
      onCompleteThenStop()
    case Request(_) ⇒
      deliverBuf()
    case Cancel ⇒
      context.stop(self)
  }

  @inline
  private def sendPackage(sessionId: Long, messageId: Long, message: ProtoMessage) = {
    val mbBytes = MessageBoxCodec.encode(MessageBox(messageId, message)).require
    enqueue(MTPackage(authId, sessionId, mbBytes))
  }

  private def handleMessageBox(pAuthId: Long, pSessionId: Long, mb: MessageBox): Unit = {

    def withValidKey(keyId: Long)(f: ServerKey ⇒ Any): Unit =
      serverKeys find (_.id == keyId) match {
        case Some(serverKey) ⇒ f(serverKey)
        case None            ⇒ self ! SendDrop(mb.messageId, "Wrong key id")
      }

    def withValidSession(randomId: Long)(f: BitVector ⇒ Any): Unit =
      sessions get randomId match {
        case Some(serverNonce) ⇒ f(serverNonce)
        case None              ⇒ self ! SendDrop(mb.messageId, "Wrong random id")
      }

    def withValidNonce(nonce: BitVector)(f: ⇒ Any): Unit =
      if (nonce.bytes.length == NonceBytes) f
      else self ! SendDrop(mb.messageId, "Wrong nonce")

    def handleRequestAuthId(): Unit = {
      val f =
        if (authId == 0L) {
          authId = IdUtils.nextAuthId(ThreadLocalSecureRandom.current())
          db.run(persist.AuthIdRepo.create(authId, None, None))
        } else Future.successful(())

      f onComplete {
        case Success(_) ⇒
          self ! SendPackage(pSessionId, mb.messageId, ResponseAuthId(authId))
        case Failure(e) ⇒ self ! SendDrop(mb.messageId, "Failed to create AuthId")
      }
    }

    def handleRequestStartAuth(randomId: Long): Unit = {
      val nonceBytes = new Array[Byte](NonceBytes)
      new SecureRandom().nextBytes(nonceBytes)
      val nonce = BitVector(nonceBytes)
      this.sessions += randomId → nonce
      val rsp = ResponseStartAuth(randomId, serverKeys.toVector map (_.id), nonce)
      sendPackage(pSessionId, mb.messageId, rsp)
    }

    def handleRequestGetServerKey(keyId: Long): Unit = {
      withValidKey(keyId) { serverKey ⇒
        val rsp = ResponseGetServerKey(keyId, BitVector(serverKey.public))
        sendPackage(pSessionId, mb.messageId, rsp)
      }
    }

    def handleRequestDH(randomId: Long, keyId: Long, clientNonce: BitVector, clientKey: BitVector): Unit = {
      withValidKey(keyId) { serverKey ⇒
        withValidSession(randomId) { serverNonce ⇒
          withValidNonce(clientNonce) {
            val preMaster = Curve25519.calculateAgreement(serverKey.privat, clientKey.toByteArray)
            val fullNonce = (clientNonce ++ serverNonce).toByteArray
            val prfCombined = Cryptos.PRF_SHA_STREEBOG_256()
            val master = prfCombined.calculate(preMaster, "master secret", fullNonce, PRFBytes)
            val verify = prfCombined.calculate(master, "client finished", fullNonce, PRFBytes)
            val randomBytes = new Array[Byte](SignRandomBytes)
            new SecureRandom().nextBytes(randomBytes)
            val verifySign = Curve25519.calculateSignature(randomBytes, serverKey.privat, verify)

            db.run((
              for {
                masterKey ← MasterKeyRepo.create(master)
                _ ← AuthIdRepo.create(masterKey.authId)
              } yield masterKey
            ).transactionally)
              .map { masterKey ⇒
                SendPackage(pSessionId, mb.messageId, ResponseDoDH(randomId, BitVector(verify), BitVector(verifySign)))
              }
              .pipeTo(self)
          }
        }
      }
    }

    if (pAuthId == 0L) {
      if (pSessionId != 0L) self ! SendDrop(mb.messageId, "sessionId must be equal to zero")
      else mb.body match {
        case RequestAuthId ⇒ handleRequestAuthId()
        case RequestStartAuth(randomId) ⇒ handleRequestStartAuth(randomId)
        case RequestGetServerKey(keyId) ⇒ handleRequestGetServerKey(keyId)
        case RequestDH(randomId, keyId, clientNonce, clientKey) ⇒ handleRequestDH(randomId, keyId, clientNonce, clientKey)
        case _ ⇒ self ! SendDrop(mb.messageId, "Unknown request")
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

  override def unhandled(message: Any): Unit = {
    super.unhandled(message)
    log.error("Unhandled message: {}", message)
  }
}
