package im.actor.server.api.frontend

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
import im.actor.server.session.SessionRegion
import im.actor.server.util.streams.SourceWatchManager

object MTProto {

  import akka.pattern.{ AskTimeoutException, ask }
  import akka.stream.stage._

  val protoVersions: Set[Byte] = Set(1)
  val apiMajorVersions: Set[Byte] = Set(1)

  def flow(maxBufferSize: Int, sessionRegion: SessionRegion)(implicit db: Database, system: ActorSystem, timeout: Timeout) = {
    val authManager = system.actorOf(AuthorizationManager.props(db))
    val authSource = Source(ActorPublisher[MTTransport](authManager))

    val sessionClient = system.actorOf(SessionClient.props(sessionRegion))
    val sessionClientSource = Source(ActorPublisher[MTTransport](sessionClient))

    val (watchManager, watchSource) = SourceWatchManager[MTTransport](authManager)

    val mtprotoFlow = Flow[ByteString]
      .transform(() ⇒ MTProto.parse(maxBufferSize))
      .mapAsyncUnordered(MTProto.handlePackage(_, authManager, sessionClient))
      .mapConcat(msgs ⇒ msgs.toVector)

    val mapRespFlow = Flow[MTTransport]
      .transform(() ⇒ MTProto.mapResponse(system))

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
      val merge = builder.add(Merge[MTTransport](4))

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

  def parse(maxBufferSize: Int)(implicit system: ActorSystem) = new StatefulStage[ByteString, (MTTransport, Option[Int])] {

    sealed trait ParserStep

    @SerialVersionUID(1L)
    case object HandshakeHeader extends ParserStep

    @SerialVersionUID(1L)
    case class HandshakeData(header: HandshakeHeader) extends ParserStep

    @SerialVersionUID(1L)
    case object AwaitPackageHeader extends ParserStep

    @SerialVersionUID(1L)
    case class AwaitPackageBody(header: TransportPackageHeader) extends ParserStep

    @SerialVersionUID(1L)
    case class FailedState(msg: String) extends ParserStep

    type ParseState = (ParserStep, BitVector)

    private[this] var parserState: ParseState = (HandshakeHeader, BitVector.empty)

    @inline
    private def failedState(msg: String) = ((FailedState(msg), BitVector.empty), Vector.empty)

    def initial = new State {
      override def onPush(chunk: ByteString, ctx: Context[(MTTransport, Option[Int])]): Directive = {
        val (newState, res) = doParse(parserState._1, parserState._2 ++ BitVector(chunk.toByteBuffer))(Vector.empty)
        newState._1 match {
          case FailedState(msg) ⇒
            system.log.debug("Failed to parse connection-level {}", msg)
            // ctx.fail(new IllegalStateException(msg))
            ctx.pushAndFinish((ProtoPackage(Drop(0, 0, msg)), None))
          case _ ⇒
            parserState = newState
            emit(res.iterator, ctx)
        }
      }

      @tailrec
      private def doParse(state: ParserStep, buf: BitVector)(pSeq: Vector[(MTTransport, Option[Int])]): (ParseState, Vector[(MTTransport, Option[Int])]) = {
        if (buf.isEmpty) {
          ((state, buf), pSeq)
        } else state match {
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
                  doParse(AwaitPackageHeader, remainder)(pSeq :+ ((ProtoPackage(body), Some(index))))
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

  def mapResponse(system: ActorSystem) = new PushPullStage[MTTransport, ByteString] {
    private[this] var packageIndex: Int = -1

    override def onPush(elem: MTTransport, ctx: Context[ByteString]): Directive = elem match {
      case h @ Handshake(protoVersion, apiMajorVersion, _, _) ⇒
        val resBits = handshakeResponse.encode(h).require
        val res = ByteString(resBits.toByteBuffer)
        // system.log.debug("Sending bytes {}", resBits)

        if (protoVersion == 0 || apiMajorVersion == 0) {
          ctx.pushAndFinish(res)
        }

        ctx.push(res)
      case ProtoPackage(p) ⇒
        packageIndex += 1
        val pkg = TransportPackage(packageIndex, p)
        // system.log.debug("Sending TransportPackage {}", pkg)

        val resBits = TransportPackageCodec.encode(pkg).require
        val res = ByteString(resBits.toByteBuffer)
        // system.log.debug("Sending bytes {}", resBits)

        p match {
          case _: Drop ⇒
            ctx.pushAndFinish(res)
          case _ ⇒
            ctx.push(res)
        }
      case SilentClose ⇒ ctx.finish()
    }

    override def onPull(ctx: Context[ByteString]): Directive = ctx.pull()
  }

  def handlePackage(req: (MTTransport, Option[Int]), authManager: ActorRef, sessionClient: ActorRef)(implicit system: ActorSystem, timeout: Timeout): Future[Seq[MTTransport]] = {
    import system.dispatcher

    req match {
      case (p @ ProtoPackage(body), optIndex) ⇒
        @inline def withAck(p: ProtoPackage): Seq[ProtoPackage] =
          ackSeq() :+ p

        @inline def ackSeq(): Seq[ProtoPackage] =
          optIndex.map(index ⇒ Seq(ProtoPackage(Ack(index)))).getOrElse(Seq.empty)

        body match {
          case m: MTPackage ⇒
            if (m.authId == 0) {
              authManager.ask(AuthorizationManager.FrontendPackage(m)).mapTo[MTTransport].map(Seq(_)).recover {
                case e: AskTimeoutException ⇒
                  val msg = s"handleAsk within $timeout"
                  system.log.error(e, msg)
                  withAck(ProtoPackage(InternalError(0, 0, msg))) // FIXME: send drop
              }
            } else {
              sessionClient ! SessionClient.SendToSession(m)

              Future.successful(ackSeq())
            }
          case Ping(bytes) ⇒ Future.successful(withAck(ProtoPackage(Pong(bytes))))
          case Pong(bytes) ⇒ Future.successful(ackSeq())
          case m           ⇒ Future.successful(ackSeq())
        }
      case (h: Handshake, _) ⇒ Future.successful {
        val sha256Sign = BitVector(DigestUtils.sha256(h.bytes.toByteArray))
        val protoVersion: Byte = if (protoVersions.contains(h.protoVersion)) h.protoVersion else 0
        val apiMajorVersion: Byte = if (apiMajorVersions.contains(h.apiMajorVersion)) h.apiMajorVersion else 0
        val apiMinorVersion: Byte = if (apiMajorVersion == 0) 0 else h.apiMinorVersion
        Seq(Handshake(protoVersion, apiMajorVersion, apiMinorVersion, sha256Sign))
      }
      case (SilentClose, _) ⇒
        system.log.debug("SilentClose")
        // FIXME: do the real silent close?
        throw new Exception("SilentClose handler is not implemented")
    }
  }
}
