package im.actor.server.frontend

import java.net.InetAddress

import akka.actor._
import akka.pattern.pipe
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{ Cancel, Request }
import com.google.protobuf.ByteString
import im.actor.crypto.box.CBCHmacBox
import im.actor.crypto.primitives.kuznechik.KuznechikFastEngine
import im.actor.crypto.primitives.streebog.Streebog256
import im.actor.crypto.primitives.aes.AESFastEngine
import im.actor.crypto.primitives.digest.SHA256
import im.actor.crypto.primitives.util.ByteStrings
import im.actor.crypto.ActorProtoKey
import im.actor.server.db.DbExtension
import im.actor.server.model.MasterKey
import im.actor.server.mtproto.codecs.protocol._
import im.actor.server.mtproto.protocol._
import im.actor.server.mtproto.transport.Drop
import im.actor.server.mtproto.{ transport ⇒ T }
import im.actor.server.persist.{ AuthIdRepo, MasterKeyRepo }
import im.actor.server.session.{ HandleMessageBox, SessionEnvelope, SessionRegion }
import im.actor.util.ThreadLocalSecureRandom
import scodec.{ Attempt, DecodeResult }
import scodec.bits.BitVector
import slick.dbio.DBIO

import scala.annotation.tailrec
import scala.collection.immutable
import scala.util.{ Failure, Success, Try }

final case class InvalidSeq(real: Long, expected: Long) extends RuntimeException
case object EncryptedPackageDecodeError extends RuntimeException
case object EncryptedPackageDecryptError extends RuntimeException

private[frontend] object SessionClient {

  @SerialVersionUID(1L)
  final case class SendToSession(authId: Long, sessionId: Long, mbBits: BitVector)

  @SerialVersionUID(1L)
  private final case class IdsObtained(authId: Option[Either[Long, MasterKey]], sessionId: Long)

  def props(sessionRegion: SessionRegion, remoteAddr: InetAddress) = Props(classOf[SessionClient], sessionRegion, remoteAddr)
}

final class CryptoHelper(protoKeys: ActorProtoKey) {
  private def random = ThreadLocalSecureRandom.current()

  private object CbcHmac {
    object Server {
      val USA = new CBCHmacBox(
        new AESFastEngine(protoKeys.getServerKey),
        new SHA256,
        protoKeys.getServerMacKey
      )

      val Russia = new CBCHmacBox(
        new KuznechikFastEngine(protoKeys.getServerRussianKey),
        new Streebog256,
        protoKeys.getServerMacRussianKey
      )
    }

    object Client {
      val USA = new CBCHmacBox(
        new AESFastEngine(protoKeys.getClientKey),
        new SHA256,
        protoKeys.getClientMacKey
      )

      val Russia = new CBCHmacBox(
        new KuznechikFastEngine(protoKeys.getClientRussianKey),
        new Streebog256,
        protoKeys.getClientMacRussianKey
      )
    }
  }

  def decrypt(seq: Long, cbcPackageBits: BitVector): Try[BitVector] = {
    for {
      usa ← decryptUSA(seq, cbcPackageBits)
      secret ← decryptRussia(seq, usa)
    } yield secret
  }

  private def decryptUSA(seq: Long, cbcPackageBits: BitVector): Try[BitVector] =
    decrypt(seq, cbcPackageBits, CbcHmac.Client.USA)

  private def decryptRussia(seq: Long, cbcPackageBits: BitVector): Try[BitVector] =
    decrypt(seq, cbcPackageBits, CbcHmac.Client.Russia)

  private def decrypt(seq: Long, cbcPackageBits: BitVector, cbcHmac: CBCHmacBox): Try[BitVector] = {
    EncryptionCBCPackageCodec.decode(cbcPackageBits) match {
      case Attempt.Successful(DecodeResult(EncryptionCBCPackage(iv, encSecret), remainder)) ⇒
        if (remainder.isEmpty)
          Try(BitVector(cbcHmac.decryptPackage(ByteStrings.longToBytes(seq), iv.toByteArray, encSecret.toByteArray)))
        else
          Failure(EncryptedPackageDecodeError)
      case Attempt.Failure(e) ⇒ Failure(EncryptedPackageDecodeError)
    }
  }

  def encrypt(seq: Long, plaintext: BitVector): BitVector = {
    val russia = EncryptionCBCPackageCodec.encode(encryptRussia(seq, plaintext.toByteArray)).require
    EncryptionCBCPackageCodec.encode(encryptUSA(seq, russia.toByteArray)).require
  }

  private def encryptUSA(seq: Long, plaintext: Array[Byte]): EncryptionCBCPackage =
    encrypt(seq, plaintext, CbcHmac.Server.USA)

  private def encryptRussia(seq: Long, plaintext: Array[Byte]): EncryptionCBCPackage =
    encrypt(seq, plaintext, CbcHmac.Server.Russia)

  private def encrypt(seq: Long, plaintext: Array[Byte], cbcHmac: CBCHmacBox): EncryptionCBCPackage = {
    val iv = new Array[Byte](16)
    random.nextBytes(iv)

    val encrypted = cbcHmac.encryptPackage(
      ByteStrings.longToBytes(seq),
      iv,
      plaintext
    )

    EncryptionCBCPackage(BitVector(iv), BitVector(encrypted))
  }
}

private[frontend] final class SessionClient(sessionRegion: SessionRegion, remoteAddr: InetAddress)
  extends Actor
  with ActorLogging
  with ActorPublisher[T.MTProto]
  with Stash {

  import SessionClient._
  import context.dispatcher

  type PackFn = BitVector ⇒ T.MTProto

  val db = DbExtension(context.system).db

  private[this] var packageQueue = immutable.Queue.empty[T.MTProto]
  private[this] var watching = Set.empty[ActorRef]
  private[this] var seq = -1L
  private[this] var clientSeq = -1L

  def receive: Receive = waitForIds

  def waitForIds: Receive = publisher orElse {
    case s @ SendToSession(pAuthId, pSessionId, mbBits) ⇒
      stash()

      db.run(for {
        masterKeyOpt ← MasterKeyRepo.find(pAuthId)
        authId ← masterKeyOpt match {
          case Some(m) ⇒ DBIO.successful(Some(Right(m)))
          case None    ⇒ AuthIdRepo.find(pAuthId) map (_.map(a ⇒ Left(a.id)))
        }
      } yield IdsObtained(authId, pSessionId)) pipeTo self

      context become {
        case IdsObtained(Some(Left(authId)), sessionId) ⇒
          context become working(authId, sessionId, (bits: BitVector) ⇒ T.MTPackage(authId, sessionId, bits), (bits: BitVector) ⇒ Success(bits))
          unstashAll()
        case IdsObtained(Some(Right(masterKey)), sessionId) ⇒
          val crypto = new CryptoHelper(new ActorProtoKey(masterKey.body.toByteArray))

          context become working(
            masterKey.authId,
            sessionId, (bits: BitVector) ⇒ {
            this.seq += 1
            val encrPackageBits = EncryptedPackageCodec.encode(EncryptedPackage(seq, crypto.encrypt(seq, bits))).require
            T.MTPackage(masterKey.authId, sessionId, encrPackageBits)
          },
            (encryptedBits: BitVector) ⇒ {
              EncryptedPackageCodec.decode(encryptedBits) match {
                case Attempt.Successful(DecodeResult(EncryptedPackage(pSeq, mbBits), remainder)) ⇒
                  this.clientSeq += 1
                  if (pSeq != clientSeq) {
                    Failure(InvalidSeq(pSeq, clientSeq))
                  } else if (remainder.isEmpty) {
                    crypto.decrypt(pSeq, mbBits)
                  } else
                    Failure(EncryptedPackageDecodeError)
                case Attempt.Failure(err) ⇒
                  Failure(EncryptedPackageDecodeError)
              }
            }
          )
          unstashAll()
        case IdsObtained(None, _) ⇒
          enqueuePackage(T.MTPackage(pAuthId, pSessionId, MessageBoxCodec.encode(MessageBox(Long.MaxValue, AuthIdInvalid)).require))
          onCompleteThenStop()
        case Status.Failure(e) ⇒
          log.error(e, "Failed to check authId")
          onErrorThenStop(e)
        case other ⇒ stash()
      }
  }

  def working(authId: Long, sessionId: Long, pack: BitVector ⇒ T.MTProto, unpack: BitVector ⇒ Try[BitVector]): Receive = publisher orElse {
    case SendToSession(pAuthId, pSessionId, mbBits) ⇒
      if (pAuthId != authId) {
        log.warning("authId has changed")
        enqueuePackage(Drop(0, 0, "authId has changed"))
        onCompleteThenStop()
      } else if (pSessionId != sessionId) {
        log.warning("sessionId has changed")
        enqueuePackage(Drop(0, 0, "sessionId has changed"))
        onCompleteThenStop()
      } else {
        unpack(mbBits) match {
          case Success(rawBits) ⇒
            sessionRegion.ref ! SessionEnvelope(authId, sessionId).withHandleMessageBox(HandleMessageBox(ByteString.copyFrom(rawBits.toByteBuffer), Some(remoteAddr)))
          case Failure(EncryptedPackageDecodeError) ⇒
            enqueuePackage(Drop(0, 0, "Cannot parse EncryptedPackage"))
            onCompleteThenStop()
          case Failure(EncryptedPackageDecryptError) ⇒
            enqueuePackage(Drop(0, 0, "Cannot decrypt EncryptedPackage"))
            onCompleteThenStop()
          case Failure(InvalidSeq(real, expected)) ⇒
            enqueuePackage(Drop(0, 0, s"Invalid seq: $real, expected: $expected"))
            onCompleteThenStop()
          case Failure(e) ⇒
            val message = "Cannot decode Package body"
            log.error(e, message)
            enqueuePackage(Drop(0, 0, message))
        }
      }
    case mbBits: BitVector ⇒
      if (!watching.contains(sender())) {
        context watch sender()
        watching += sender()
      }
      enqueuePackage(pack(mbBits))
    case Terminated(sessionRef) ⇒
      val p = pack(MessageBoxCodec.encode(MessageBox(Long.MaxValue, SessionLost)).require)
      enqueuePackage(p)
  }

  def publisher: Receive = {
    case Request(_) ⇒
      deliverBuf()
    case Cancel ⇒
      context.stop(self)
  }

  private def enqueuePackage(p: T.MTProto): Unit = {
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

  override def unhandled(message: Any): Unit = {
    super.unhandled(message)
    log.error("Unhandled message: {}", message)
  }
}
