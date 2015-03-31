package im.actor.server.api.service

import scala.annotation.tailrec
import scala.concurrent.Future

import akka.actor._
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl._
import akka.util.{ ByteString, Timeout }
import org.apache.commons.codec.digest.DigestUtils
import scodec.DecodeResult
import scodec.bits.BitVector
import slick.driver.PostgresDriver.api.Database

import im.actor.server.mtproto.codecs._
import im.actor.server.mtproto.codecs.transport._
import im.actor.server.mtproto.transport._
import im.actor.server.util.streams.SourceWatchManager

object MTProto {

  import akka.pattern.{ AskTimeoutException, ask }
  import akka.stream.stage._

  val protoVersions: Set[Byte] = Set(1)
  val apiMajorVersions: Set[Byte] = Set(1)

  def flow(maxBufferSize: Int)
          (implicit db: Database, system: ActorSystem, timeout: Timeout) = {
    val authManager = system.actorOf(AuthorizationManager.props(db))
    val authSource = Source(ActorPublisher[MTTransport](authManager))
    val (watchManager, watchSource) = SourceWatchManager[MTTransport](authManager)

    val mtprotoFlow = Flow[ByteString]
      .transform(() => MTProto.parse(maxBufferSize))
      .mapAsyncUnordered(MTProto.handlePackage(_, authManager))
    val mapRespFlow = Flow[MTTransport]
      .transform(() => MTProto.mapResponse())
    val completeSink = Sink.onComplete {
      case _ =>
        watchManager ! PoisonPill
        authManager ! PoisonPill
    }

    Flow() { implicit builder =>
      import FlowGraph.Implicits._

      val bcast = builder.add(Broadcast[ByteString](2))
      val merge = builder.add(Merge[MTTransport](3))

      val mtproto = builder.add(mtprotoFlow)
      val auth = builder.add(authSource)
      val watch = builder.add(watchSource)
      val mapResp = builder.add(mapRespFlow)
      val complete = builder.add(completeSink)

      // @formatter:off

      mtproto ~> merge
      auth    ~> merge
      watch   ~> merge
                 merge ~> mapResp ~> bcast
                                     bcast.out(0) ~> complete

      // @formatter:on

      (mtproto.inlet, bcast.out(1))
    }
  }

  def parse(maxBufferSize: Int)(implicit system: ActorSystem) = new StatefulStage[ByteString, MTTransport] {

    sealed trait ParserStep

    @SerialVersionUID(1L)
    case object HandshakeStep extends ParserStep

    @SerialVersionUID(1L)
    case object AwaitPackageHeader extends ParserStep

    @SerialVersionUID(1L)
    case class AwaitPackageBody(header: TransportPackageHeader) extends ParserStep

    @SerialVersionUID(1L)
    case class FailedState(msg: String) extends ParserStep

    type ParseState = (ParserStep, BitVector)

    private[this] var parserState: ParseState = (HandshakeStep, BitVector.empty)

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
        if (buf.isEmpty) {
          ((state, buf), pSeq)
        } else state match {
          case HandshakeStep =>
            HandshakeCodec.decode(buf).toEither match {
              case Right(res) =>
                system.log.debug("Handshake {}", res.value)
                ((AwaitPackageHeader, res.remainder), Vector(res.value))
              case Left(e) =>
                system.log.debug("Failed to parse Handshake: {}", e)
                failedState(e.message)
            }
          case AwaitPackageHeader =>
            if (buf.length < transportPackageHeader.sizeBound.lowerBound) {
              ((AwaitPackageHeader, buf), pSeq)
            } else {
              transportPackageHeader.decode(buf).toEither match {
                case Right(headerRes) =>
                  doParse(AwaitPackageBody(headerRes.value), headerRes.remainder)(pSeq)
                case Left(e) => failedState(e.message)
              }
            }
          case state @ AwaitPackageBody(TransportPackageHeader(index, header, size)) =>
            val bitsLength = size * byteSize + int32Bits

            if (buf.size < bitsLength) {
              ((state, buf), pSeq)
            } else {
              val (body, remainder) = buf.splitAt(bitsLength)

              (new SignedMTProtoDecoder(header, size)).decode(body).toEither match {
                case Right(DecodeResult(body, BitVector.empty)) =>
                  doParse(AwaitPackageHeader, remainder)(pSeq :+ ProtoPackage(body))
                case Right(_) =>
                  failedState("Body length is more than body itself")
                case Left(e) =>
                  failedState(e.message)
              }
            }
          case _: FailedState =>
            ((state, BitVector.empty), Vector.empty)
        }

      }
    }
  }

  def mapResponse() = new PushPullStage[MTTransport, ByteString] {
    private[this] var packageIndex: Int = 0

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
        val clientBytes = BitVector(h.protoVersion, h.apiMajorVersion, h.apiMinorVersion) ++ h.bytes
        val sha1Sign = BitVector(DigestUtils.sha1(clientBytes.toByteArray))
        val protoVersion: Byte = if (protoVersions.contains(h.protoVersion)) h.protoVersion else 0
        val apiMajorVersion: Byte = if (apiMajorVersions.contains(h.apiMajorVersion)) h.apiMajorVersion else 0
        val apiMinorVersion: Byte = if (apiMajorVersion == 0) 0 else h.apiMinorVersion
        Handshake(protoVersion, apiMajorVersion, apiMinorVersion, sha1Sign)
      }
    }
  }
}
