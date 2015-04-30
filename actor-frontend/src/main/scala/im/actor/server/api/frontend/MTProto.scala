package im.actor.server.api.frontend

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext

import akka.actor._
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl._
import akka.util.{ ByteString, Timeout }
import scodec.DecodeResult
import scodec.bits.BitVector
import slick.driver.PostgresDriver.api.Database

import im.actor.server.mtproto.codecs._
import im.actor.server.mtproto.codecs.transport._
import im.actor.server.mtproto.transport._
import im.actor.server.session.SessionRegion
import im.actor.server.util.streams.SourceWatchManager

object MTProto {

  import akka.stream.stage._

  val protoVersions: Set[Byte] = Set(1)
  val apiMajorVersions: Set[Byte] = Set(1)
  val mapParallelism = 4 // TODO: #perf tune up and make it configurable

  def flow(maxBufferSize: Int, sessionRegion: SessionRegion)(implicit db: Database, system: ActorSystem, timeout: Timeout) = {
    val authManager = system.actorOf(AuthorizationManager.props(db))
    val authSource = Source(ActorPublisher[MTProto](authManager))

    val sessionClient = system.actorOf(SessionClient.props(sessionRegion))
    val sessionClientSource = Source(ActorPublisher[MTProto](sessionClient))

    val (watchManager, watchSource) = SourceWatchManager[MTProto](authManager)

    val mtprotoFlow: Flow[ByteString, MTProto, Unit] = Flow[ByteString]
      .transform(() ⇒ MTProto.parsePackage(maxBufferSize))
      .transform(() ⇒ new PackageCheckStage)
      .transform(() ⇒ new PackageHandleStage(protoVersions, apiMajorVersions, authManager, sessionClient))
      .mapAsync(mapParallelism)(identity)

    val mapRespFlow: Flow[MTProto, ByteString, Unit] = Flow[MTProto]
      .transform(() ⇒ mapResponse(system))

    val completeSink = Sink.onComplete {
      case x ⇒
        system.log.debug("Completing {}", x)
        watchManager ! PoisonPill
        authManager ! PoisonPill
        sessionClient ! PoisonPill
    }

    Flow() { implicit builder ⇒
      import FlowGraph.Implicits._

      val bcast = builder.add(Broadcast[ByteString](2))
      val merge = builder.add(Merge[MTProto](4))

      val mtproto = builder.add(mtprotoFlow)
      val auth = builder.add(authSource)
      val session = builder.add(sessionClientSource)
      val watch = builder.add(watchSource)
      val mapResp = builder.add(mapRespFlow)
      val complete = builder.add(completeSink)

      // @formatter:off

      mtproto ~> merge
      auth ~> merge
      session ~> merge
      watch ~> merge
      merge ~> mapResp ~> bcast
      bcast.out(0) ~> complete

      // @formatter:on

      (mtproto.inlet, bcast.out(1))
    }
  }

  def parsePackage(maxBufferSize: Int)(implicit system: ActorSystem) = new StatefulStage[ByteString, TransportPackage] {

    sealed trait ParserStep

    @SerialVersionUID(1L)
    case object AwaitPackageHeader extends ParserStep

    @SerialVersionUID(1L)
    case class AwaitPackageBody(header: TransportPackageHeader) extends ParserStep

    @SerialVersionUID(1L)
    case class FailedState(msg: String) extends ParserStep

    type ParseState = (ParserStep, BitVector)

    private[this] var parserState: ParseState = (AwaitPackageHeader, BitVector.empty)

    @inline
    private def failedState(msg: String) = ((FailedState(msg), BitVector.empty), Vector.empty)

    def initial = new State {
      override def onPush(chunk: ByteString, ctx: Context[TransportPackage]) = {
        val (newState, res) = doParse(parserState._1, parserState._2 ++ BitVector(chunk.toByteBuffer))(Vector.empty)
        newState._1 match {
          case FailedState(msg) ⇒
            system.log.debug("Failed to parse connection-level {}", msg)
            // ctx.fail(new IllegalStateException(msg))
            ctx.pushAndFinish(TransportPackage(0, Drop(0, 0, msg)))
          case _ ⇒
            parserState = newState
            emit(res.iterator, ctx)
        }
      }

      @tailrec
      private def doParse(state: ParserStep, buf: BitVector)(pSeq: Vector[TransportPackage]): (ParseState, Vector[TransportPackage]) = {
        if (buf.isEmpty) {
          ((state, buf), pSeq)
        } else state match {
          /*
          case HandshakeHeader ⇒
            if (buf.length >= handshakeHeaderSize) {
              handshakeHeader.decode(buf).toEither match {
                case Right(res) ⇒
                  // system.log.debug("Handshake header {}", res.value)
                  doParse(HandshakeData(res.value), res.remainder)(pSeq)
                case Left(e) ⇒
                  // system.log.debug("Failed to parse Handshake header {}", e)
                  failedState(e.message)
              }
            } else {
              ((HandshakeHeader, buf), pSeq)
            }
          case state @ HandshakeData(header) ⇒
            // FIXME: compute once
            // FIXME: check if fits into Int
            val bitsLength = (header.dataLength * byteSize).toInt

            if (buf.length >= bitsLength) {
              handshakeData(header.dataLength).decode(buf).toEither match {
                case Right(res) ⇒
                  // system.log.debug("Handshake data {}", res)
                  val handshake = Handshake(header.protoVersion, header.apiMajorVersion, header.apiMinorVersion, res.value)
                  doParse(AwaitPackageHeader, res.remainder)(Vector((handshake, None)))
                case Left(e) ⇒
                  // system.log.debug("Failed to parse handshake data: {}", e)
                  failedState(e.message)
              }
            } else {
              ((state, buf), pSeq)
            }
            */
          case AwaitPackageHeader ⇒
            if (buf.length < transportPackageHeader.sizeBound.lowerBound) {
              ((AwaitPackageHeader, buf), pSeq)
            } else {
              transportPackageHeader.decode(buf).toEither match {
                case Right(headerRes) ⇒
                  // system.log.debug("Transport package header {}", headerRes)
                  doParse(AwaitPackageBody(headerRes.value), headerRes.remainder)(pSeq)
                case Left(e) ⇒
                  // system.log.debug("failed to parse package header", e)
                  failedState(e.message)
              }
            }
          case state @ AwaitPackageBody(TransportPackageHeader(index, header, size)) ⇒
            val bitsLength = size * byteSize + int32Bits

            if (buf.size < bitsLength) {
              ((state, buf), pSeq)
            } else {
              val (body, remainder) = buf.splitAt(bitsLength)
              (new SignedMTProtoDecoder(header, size)).decode(body).toEither match {
                case Right(DecodeResult(body, BitVector.empty)) ⇒
                  doParse(AwaitPackageHeader, remainder)(pSeq :+ TransportPackage(index, body))
                case Right(_) ⇒
                  failedState("Body length is more than body itself")
                case Left(e) ⇒
                  failedState(e.message)
              }
            }
          case _: FailedState ⇒
            ((state, BitVector.empty), Vector.empty)
        }
      }
    }
  }

  def mapResponse(system: ActorSystem) = new PushPullStage[MTProto, ByteString] {
    private[this] var packageIndex: Int = -1

    override def onPush(elem: MTProto, ctx: Context[ByteString]) = {
      packageIndex += 1
      val pkg = TransportPackage(packageIndex, elem)
      // system.log.debug("Sending TransportPackage {}", pkg)

      val resBits = TransportPackageCodec.encode(pkg).require
      val res = ByteString(resBits.toByteBuffer)
      // system.log.debug("Sending bytes {}", resBits)

      elem match {
        case _: Drop ⇒
          ctx.pushAndFinish(res)
        case _ ⇒
          ctx.push(res)
      }
    }

    override def onPull(ctx: Context[ByteString]) = ctx.pull()
  }
}
