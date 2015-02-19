package im.actor.server.api.service

import im.actor.server.mtproto.codecs._
import im.actor.server.mtproto.codecs.transport._
import im.actor.server.mtproto.transport._
import akka.actor._
import akka.stream.actor.ActorPublisher
import akka.util.{ ByteString, Timeout }
import akka.stream.scaladsl._
import scala.annotation.tailrec
import scala.concurrent.Future
import scodec.bits.BitVector
import scodec.codecs
import org.apache.commons.codec.digest.DigestUtils

object MTProto {
  import akka.pattern.{ ask, AskTimeoutException }
  import akka.stream.stage._

  val protoVersions: Set[Byte] = Set(1)
  val apiMajorVersions: Set[Byte] = Set(1)

  def flow(maxBufferSize: Int)
          (implicit system: ActorSystem, timeout: Timeout): Flow[ByteString, ByteString] = {
    val authManager = system.actorOf(AuthorizationManager.props())
    val auth = Source(ActorPublisher[MTTransport](authManager))
    val (watchManager, watch) = SourceWatchManager[MTTransport](authManager)

    val mtproto = Flow[ByteString]
      .transform(() => MTProto.parse(maxBufferSize))
      .mapAsyncUnordered(MTProto.handlePackage(_, authManager))
    val mapResp = Flow[MTTransport]
      .transform(() => MTProto.mapResponse())
    val complete = Sink.onComplete {
      case _ =>
        watchManager ! PoisonPill
        authManager ! PoisonPill
    }
    Flow[ByteString, ByteString]() { implicit b =>
      import FlowGraphImplicits._

      val in = UndefinedSource[ByteString]
      val out = UndefinedSink[ByteString]
      val bcast = Broadcast[ByteString]
      val merge = Merge[MTTransport]

      in ~> mtproto ~> merge
      auth          ~> merge
      watch         ~> merge
                       merge ~> mapResp ~> bcast
                                           bcast ~> out
                                           bcast ~> complete

      (in, out)
    }
  }

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
            HandshakeCodec.decode(buf).toEither match {
              case Right(res) => ((AwaitPackage, res.remainder), Vector(res.value))
              case Left(e) => failedState(e.message)
            }
          case AwaitPackage =>
            if (buf.length < int32Bits) ((AwaitPackage, buf), pSeq)
            else {
              codecs.int32.decode(buf).toEither match {
                case Right(lenRes) =>
                  if (lenRes.value < (lenRes.remainder.length / byteSize))
                    ((AwaitBytes(lenRes.value), lenRes.remainder), pSeq)
                  else {
                    TransportPackageCodec.decode(lenRes.remainder).toEither match {
                      case Right(res) => doParse(AwaitPackage, res.remainder)(pSeq.:+(ProtoPackage(res.value.pkg)))
                      case Left(e) => failedState(e.message)
                    }
                  }
                case Left(e) => failedState(e.message)
              }
            }
          case AwaitBytes(awaitLength) =>
            if (awaitLength > (buf.length / byteSize)) ((AwaitBytes(awaitLength), buf), pSeq)
            else {
              TransportPackageCodec.decode(buf).toEither match {
                case Right(res) => doParse(AwaitPackage, res.remainder)(pSeq.:+(ProtoPackage(res.value.pkg)))
                case Left(e) => failedState(e.message)
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
        val res = ByteString(HandshakeCodec.encode(h).require.toByteBuffer)
        if (protoVersion == 0 || apiMajorVersion == 0) ctx.pushAndFinish(res)
        ctx.push(res)
      case ProtoPackage(p) =>
        packageIndex += 1
        val pkg = TransportPackage(packageIndex, p)
        val res = ByteString(TransportPackageCodec.encode(pkg).require.toByteBuffer)
        p match {
          case _: Drop => ctx.pushAndFinish(res)
          case _ => ctx.push(res)
        }
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
            actorRef.ask(AuthorizationManager.FrontendPackage(m)).mapTo[MTTransport].recover {
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
        val clientBytes = BitVector(h.protoVersion, h.apiMajorVersion, h.apiMinorVersion) ++ h.randomBytes
        val sha1Sign = BitVector(DigestUtils.sha1(clientBytes.toByteArray))
        val protoVersion: Byte = if (protoVersions.contains(h.protoVersion)) h.protoVersion else 0
        val apiMajorVersion: Byte = if (apiMajorVersions.contains(h.apiMajorVersion)) h.apiMajorVersion else 0
        val apiMinorVersion: Byte = if (apiMajorVersion == 0) 0 else h.apiMinorVersion
        Handshake(protoVersion, apiMajorVersion, apiMinorVersion, sha1Sign)
      }
    }
  }
}
