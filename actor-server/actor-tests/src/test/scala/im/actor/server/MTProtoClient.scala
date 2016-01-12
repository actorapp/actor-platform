package im.actor.server

import java.net.InetSocketAddress

import scala.concurrent.Await
import scala.concurrent.duration._

import akka.actor._
import akka.io.{ IO, Tcp }
import akka.pattern.{ ask, gracefulStop }
import akka.util.{ ByteString, Timeout }
import scodec.bits.BitVector
import scodec.{ Attempt, DecodeResult, codecs ⇒ C }

import im.actor.server.mtproto.codecs.transport._
import im.actor.server.mtproto.transport._

object MTProtoClient {
  def apply()(implicit system: ActorSystem) = new MTProtoClient(system)
}

class MTProtoClient(system: ActorSystem) {

  import MTProtoClientActor._

  private val clientActor = system.actorOf(MTProtoClientActor.props)

  def connectAndHandshake(remote: InetSocketAddress)(implicit timeout: Timeout = Timeout(10.seconds)) = {
    val res = Await.result(clientActor.ask(MTConnect(remote)), timeout.duration)

    if (res != MTConnected) {
      throw new Exception("Can not connect")
    }
  }

  def close()(implicit timeout: Timeout = Timeout(10.seconds)) = {
    val stopped = Await.result(gracefulStop(clientActor, timeout.duration), timeout.duration)

    if (!stopped) {
      throw new Exception("Closing timed out")
    }
  }

  def send(tp: MTProto, slowly: Boolean = false): Unit = {
    clientActor ! Send(tp, slowly)
  }

  def receiveTransportPackage()(implicit timeout: Timeout = Timeout(10.seconds)): Option[TransportPackage] = {
    Await.result(clientActor.ask(GetTransportPackage)(timeout).mapTo[Option[TransportPackage]], timeout.duration)
  }
}

object MTProtoClientActor {
  private val transportPackageList = C.list(TransportPackageCodec)

  case class MTConnect(remote: InetSocketAddress)

  case object MTConnected

  case class Send(p: MTProto, slowly: Boolean)

  case object GetTransportPackage

  val props = Props(classOf[MTProtoClientActor])
}

class MTProtoClientActor extends Actor with ActorLogging {

  import Tcp._
  import context.system

  import MTProtoClientActor._

  def receive: Receive = {
    case MTConnect(remote) ⇒
      val caller = sender()

      IO(Tcp) ! Connect(remote)
      context.become(connecting(caller), discardOld = true)
  }

  def connecting(caller: ActorRef): Receive = {
    case Connected(_, _) ⇒
      val connection = sender()
      connection ! Register(self)

      sendHandshake(connection)
      context.become(handshaking(caller, connection, BitVector.empty))
  }

  def handshaking(caller: ActorRef, connection: ActorRef, buffer: BitVector): Receive = {
    case Received(bs) ⇒
      val newBuffer = buffer ++ BitVector(bs.asByteBuffer)

      TransportPackageCodec.decode(newBuffer) match {
        case Attempt.Successful(DecodeResult(TransportPackage(_, hs: HandshakeResponse), remainder)) ⇒
          caller ! MTConnected
          context.become(receiving(connection, remainder, Seq.empty, Seq.empty), discardOld = true)
        case Attempt.Successful(unmatched) ⇒
          log.error("Unmatched {}", unmatched)
          self ! PoisonPill
        case Attempt.Failure(_) ⇒
          context.become(handshaking(caller, connection, newBuffer), discardOld = true)
      }
    case CommandFailed(cmd) ⇒
      log.error("Command failed while handshaking {}", cmd)
  }

  def receiving(connection: ActorRef, buffer: BitVector, tps: Seq[TransportPackage], consumers: Seq[ActorRef]): Receive = {
    case Send(p, slowly) ⇒
      send(connection, p, slowly)
    case GetTransportPackage ⇒
      if (tps.isEmpty || !consumers.isEmpty) {
        context.become(receiving(connection, buffer, tps, consumers :+ sender()), discardOld = true)
      } else {
        sender() ! tps.headOption

        context.become(receiving(connection, buffer, tps.tail, consumers), discardOld = true)
      }
    case Received(bs) ⇒
      log.debug("Received {}", bs)
      val newBuffer = buffer ++ BitVector(bs.asByteBuffer)

      transportPackageList.decode(newBuffer).recover {
        case _ ⇒ DecodeResult(Seq.empty, newBuffer)
      } match {
        case Attempt.Successful(DecodeResult(decodedTps, remainder)) ⇒
          val decodedTpsWithoutAcks = decodedTps filterNot (_.body.isInstanceOf[Ack])

          if (!consumers.isEmpty) {
            val newTps = tps ++ decodedTpsWithoutAcks

            newTps match {
              case tp :: tps ⇒
                consumers.head ! Some(tp)
                context.become(receiving(connection, buffer, tps, consumers.tail), discardOld = true)
              case Nil ⇒
            }
          } else {
            context.become(receiving(connection, buffer, tps ++ decodedTpsWithoutAcks, consumers), discardOld = true)
          }
        case Attempt.Failure(e) ⇒ // should never happen
          log.error("Failed to decode TransportPackage: {}", e)
          throw new Exception("Failed to decode TransportPackage")
      }
    case CommandFailed(w: Write) ⇒
      log.error("Failed to write {}", w.data)
  }

  private def deliverTps(connection: ActorRef, buffer: BitVector, tps: Seq[TransportPackage], consumers: Seq[ActorRef]): Unit = {
    if (!tps.isEmpty && !consumers.isEmpty) {
      consumers.head ! tps.headOption
    }

    context.become(receiving(connection, buffer, tps, consumers), discardOld = true)
  }

  private def sendHandshake(connection: ActorRef): Unit = {
    val protoVersion = 1.toByte
    val apiMajorVersion = 1.toByte
    val apiMinorVersion = 1.toByte
    val randomBytes = BitVector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

    // TODO: check digest
    //val randomBytesDigest = DigestUtils.sha1(Array(protoVersion, apiMajorVersion, apiMinorVersion) ++ randomBytes)
    val handshake = Handshake(protoVersion, apiMajorVersion, apiMinorVersion, randomBytes)
    log.debug("Sending handshake {}", handshake)
    send(connection, handshake, slowly = false)
  }

  private def send(connection: ActorRef, mtp: MTProto, slowly: Boolean): Unit = {
    val bits = TransportPackageCodec.encode(TransportPackage(1, mtp)).require

    val data = ByteString(bits.toByteBuffer)

    if (!slowly) {
      log.debug("Writing {}", data)
      connection ! Write(data)
    } else {
      log.debug("Writing slowly {}", data)

      data.grouped(5) foreach { chunk ⇒
        connection ! Write(chunk)
        Thread.sleep(5)
      }
    }
  }
}
