package im.actor.server.api.service

import akka.actor._
import akka.util.{ ByteString, Timeout }
import im.actor.server.api.mtproto.codecs.transport._
import im.actor.server.api.mtproto.transport._
import scala.annotation.tailrec
import scala.concurrent.Future
import java.security.MessageDigest
import scalaz._
import scodec.bits.BitVector

object MTProto {
  import akka.pattern.{ ask, AskTimeoutException }
  import akka.stream.stage._
  import im.actor.server.api.util.ByteConstants._

  val protoVersions = Set(1)
  val apiMajorVersions = Set(1)

  def parse(maxBufferSize: Int) = new StatefulStage[ByteString, MTTransport] {
    sealed trait ParserStep
    case object HandshakeStep extends ParserStep
    case object AwaitPackage extends ParserStep
    case class AwaitBytes(length: Int) extends ParserStep
    case class FailedState(msg: String) extends ParserStep

    type ParseState = (ParserStep, BitVector)

    private var parserState: ParseState = (HandshakeStep, BitVector.empty)

    @inline
    private def failedState(msg: String) = ((FailedState(msg), BitVector.empty), Vector.empty)

    def initial = new State {
      override def onPush(chunk: ByteString, ctx: Context[MTTransport]): Directive = {
        val (newState, res) = doParse(parserState._1, parserState._2 ++ BitVector(chunk.toByteBuffer))(Vector.empty)
        newState._1 match {
          case FailedState(msg) =>
            // ctx.fail(new IllegalStateException(msg))
            ctx.pushAndFinish(ProtoPackage(Drop(0, 0, msg)))
          case _ =>
            parserState = newState
            emit(res.iterator, ctx)
        }
      }

      @tailrec
      private def doParse(state: ParserStep, buf: BitVector)
                         (pSeq: Vector[MTTransport]): (ParseState, Vector[MTTransport]) = {
        if (buf.isEmpty) ((state, buf), pSeq)
        else state match {
          case HandshakeStep =>
            HandshakeCodec.decode(buf) match {
              case \/-((xs, h)) => ((AwaitPackage, xs), Vector(h))
              case -\/(e) => failedState(e.message)
            }
          case AwaitPackage =>
            if (buf.length < int32Bits) ((AwaitPackage, buf), pSeq)
            else {
              scodec.codecs.int32.decode(buf) match {
                case \/-((bs, pkgLength)) =>
                  if (pkgLength < (bs.length / byteSize)) ((AwaitBytes(pkgLength), bs), pSeq)
                  else {
                    TransportPackageCodec.decode(bs) match {
                      case \/-((xs, p)) => doParse(AwaitPackage, xs)(pSeq.:+(ProtoPackage(p.pkg)))
                      case -\/(e) => failedState(e.message)
                    }
                  }
                case -\/(e) => failedState(e.message)
              }
            }
          case AwaitBytes(awaitLength) =>
            if (awaitLength > (buf.length / byteSize)) ((AwaitBytes(awaitLength), buf), pSeq)
            else {
              TransportPackageCodec.decode(buf) match {
                case \/-((xs, p)) => doParse(AwaitPackage, xs)(pSeq.:+(ProtoPackage(p.pkg)))
                case -\/(e) => failedState(e.message)
              }
            }
          case _: FailedState => ((state, BitVector.empty), Vector.empty)
        }
      }
    }
  }

  def mapResponse() = new PushPullStage[MTTransport, ByteString] {
    private var packageIndex: Int = 0

    override def onPush(elem: MTTransport, ctx: Context[ByteString]): Directive = elem match {
      case h @ Handshake(protoVersion, apiMajorVersion, _, _) =>
        val res = ByteString(HandshakeCodec.encodeValid(h).toByteBuffer)
        if (protoVersion == 0 || apiMajorVersion == 0) ctx.pushAndFinish(res)
        ctx.push(res)
      case ProtoPackage(p) =>
        packageIndex += 1
        val pkg = TransportPackage(packageIndex, p)
        val res = ByteString(TransportPackageCodec.encodeValid(pkg).toByteBuffer)
        if (p.isInstanceOf[Drop]) ctx.pushAndFinish(res)
        else ctx.push(res)
      case SilentClose => ctx.finish()
    }

    override def onPull(ctx: Context[ByteString]): Directive = ctx.pull()
  }

  def handlePackage(req: MTTransport, actorRef: ActorRef)
                   (implicit system: ActorSystem, timeout: Timeout): Future[MTTransport] = {
    import system.dispatcher

    req match {
      case ProtoPackage(p) =>
        p match {
          case m: MTPackage =>
            actorRef.ask(AuthorizationActor.FrontendPackage(m)).mapTo[MTTransport].recover {
              case e: AskTimeoutException =>
                val msg = s"handleAsk within $timeout"
                system.log.error(e, msg)
                ProtoPackage(InternalError(0, 0, msg))
            }
          case Ping(bytes) => Future.successful(ProtoPackage(Pong(bytes)))
          case Pong(bytes) => Future.successful(ProtoPackage(Ping(bytes)))
          case m => Future.successful(ProtoPackage(m))
        }
      case h: Handshake => Future.successful {
        val sha1Digest = MessageDigest.getInstance("SHA1")
        val clientBytes = BitVector(h.protoVersion, h.apiMajorVersion, h.apiMinorVersion) ++ h.randomBytes
        val sha1Sign = BitVector(sha1Digest.digest(clientBytes.toByteArray))
        val protoVersion: Byte = if (protoVersions.contains(h.protoVersion)) h.protoVersion else 0
        val apiMajorVersion: Byte = if (apiMajorVersions.contains(h.apiMajorVersion)) h.apiMajorVersion else 0
        val apiMinorVersion: Byte = if (apiMajorVersion == 0) 0 else h.apiMinorVersion
        Handshake(protoVersion, apiMajorVersion, apiMinorVersion, sha1Sign)
      }
    }
  }
}
