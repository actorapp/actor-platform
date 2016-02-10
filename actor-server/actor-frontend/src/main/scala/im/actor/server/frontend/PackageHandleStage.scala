package im.actor.server.frontend

import akka.stream.{ Attributes, Outlet, Inlet, FlowShape }

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

import akka.actor.{ ActorRef, ActorSystem }
import akka.stream.stage._
import akka.util.Timeout
import org.apache.commons.codec.digest.DigestUtils
import scodec.bits.BitVector

import im.actor.server.mtproto.transport._

private[frontend] final class PackageHandleStage(
  protoVersions:    Set[Byte],
  apiMajorVersions: Set[Byte],
  authManager:      ActorRef,
  sessionClient:    ActorRef
)(implicit system: ActorSystem) extends GraphStage[FlowShape[TransportPackage, MTProto]] {

  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val askTimeout: Timeout = Timeout(5.seconds)

  private val in = Inlet[TransportPackage]("in")
  private val out = Outlet[MTProto]("out")

  override def shape = FlowShape(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    val pullIn = () ⇒ {
      if (!hasBeenPulled(in))
        pull(in)
    }

    setHandler(in, new InHandler {
      override def onPush(): Unit = grab(in) match {
        case TransportPackage(_, h: Handshake) ⇒
          push(out, {
            val sha256Sign = BitVector(DigestUtils.sha256(h.bytes.toByteArray))
            val protoVersion: Byte = if (protoVersions.contains(h.protoVersion)) h.protoVersion else 0
            val apiMajorVersion: Byte = if (apiMajorVersions.contains(h.apiMajorVersion)) h.apiMajorVersion else 0
            val apiMinorVersion: Byte = if (apiMajorVersion == 0) 0 else h.apiMinorVersion
            val hresp = HandshakeResponse(protoVersion, apiMajorVersion, apiMinorVersion, sha256Sign)
            hresp
          })

          pullIn()
        case TransportPackage(index, body) ⇒
          val ack = Ack(index)

          val fs: Seq[MTProto] = body match {
            case m: MTPackage ⇒
              if (m.authId == 0) {
                authManager ! AuthorizationManager.FrontendPackage(m)
                Seq(ack)
              } else {
                // FIXME: check authId
                sessionClient ! SessionClient.SendToSession(m.authId, m.sessionId, m.messageBytes)
                Seq(ack)
              }
            case Ping(bytes) ⇒ Seq(ack, Pong(bytes))
            case Pong(bytes) ⇒ Seq(ack)
            case m           ⇒ Seq(ack)
          }

          if (fs.nonEmpty)
            emitMultiple(out, fs.iterator, pullIn)
          else
            pullIn()
      }
    })

    setHandler(out, new OutHandler {
      override def onPull(): Unit = pullIn()
    })
  }
}
