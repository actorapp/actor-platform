package im.actor.server.api.frontend

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
)(implicit system: ActorSystem)
  extends StatefulStage[TransportPackage, MTProto] {

  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val askTimeout: Timeout = Timeout(5.seconds)

  override def initial: StageState[TransportPackage, MTProto] = new StageState[TransportPackage, MTProto] {
    override def onPush(elem: TransportPackage, ctx: Context[MTProto]): SyncDirective = {
      elem match {
        case TransportPackage(_, h: Handshake) ⇒
          ctx.push({
            val sha256Sign = BitVector(DigestUtils.sha256(h.bytes.toByteArray))
            val protoVersion: Byte = if (protoVersions.contains(h.protoVersion)) h.protoVersion else 0
            val apiMajorVersion: Byte = if (apiMajorVersions.contains(h.apiMajorVersion)) h.apiMajorVersion else 0
            val apiMinorVersion: Byte = if (apiMajorVersion == 0) 0 else h.apiMinorVersion
            val hresp = HandshakeResponse(protoVersion, apiMajorVersion, apiMinorVersion, sha256Sign)
            hresp
          })
        case TransportPackage(index, body) ⇒
          // TODO: get rid of respOptFuture and ask pattern

          val ack = Ack(index)

          val fs: Seq[MTProto] = body match {
            case m: MTPackage ⇒
              if (m.authId == 0) {
                // FIXME: remove this side effect
                authManager ! AuthorizationManager.FrontendPackage(m)
                Seq(ack)
              } else {
                sessionClient ! SessionClient.SendToSession(m)
                Seq(ack)
              }
            case Ping(bytes) ⇒ Seq(ack, Pong(bytes))
            case Pong(bytes) ⇒ Seq(ack)
            case m           ⇒ Seq(ack)
          }

          emit(fs.iterator, ctx)
      }
    }
  }
}
