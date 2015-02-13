package im.actor.server.api.frontend

import akka.actor._
import akka.util.{ ByteString, Timeout }
import akka.event.Logging
import akka.stream.scaladsl.{ Flow, ForeachSink, StreamTcp }
import akka.stream.FlowMaterializer
import im.actor.server.api.mtproto.codecs.transport._
import im.actor.server.api.mtproto.transport._
import scala.annotation.tailrec
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{ Success, Failure }
import com.typesafe.config.Config
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit
import java.security.MessageDigest
import scalaz._
import Scalaz._

object TCP {
  def start(appConf: Config)(implicit system: ActorSystem, materializer: FlowMaterializer): Unit = {
    import system.dispatcher

    val log = Logging.getLogger(system, this)
    val config = appConf.getConfig("frontend.tcp")
    implicit val askTimeout = Timeout(config.getDuration("timeout", TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS)
    val maxBufferSize = config.getBytes("max-buffer-size").toInt
    val interface = config.getString("interface")
    val port = config.getInt("port")
    val serverAddress = new InetSocketAddress(interface, port)
    val binding = StreamTcp().bind(serverAddress)

    class TestActor extends Actor {
      def receive = {
        case MTPackage(m) =>
          if (m.startsWith(ByteString("q"))) sender() ! SilentClose
          else sender() ! MTPackage(m)
      }
    }
    val actor = system.actorOf(Props(new TestActor))

    val handleFlow = Flow[ByteString]
      .transform(() => MTProto.parse(maxBufferSize))
      .mapAsyncUnordered(MTProto.handlePackage(_, actor))
      .transform(() => MTProto.mapResponse())

    val handler = ForeachSink[StreamTcp.IncomingConnection] { conn =>
      log.info(s"Client connected from: ${conn.remoteAddress}")
      conn.handleWith(handleFlow)
    }

    val materializedServer = binding.connections.to(handler).run()

    binding.localAddress(materializedServer).onComplete {
      case Success(address) =>
        log.debug(s"Server started, listening on: $address")
      case Failure(e) =>
        log.error(e, s"Server could not bind to serverAddress")
        system.shutdown()
    }
  }
}
 
object MTProto {
  import akka.pattern.{ ask, AskTimeoutException }
  import akka.stream.stage._
  import im.actor.server.api.util.ByteConstants._
  import im.actor.server.api.mtproto.codecs.CodecUtils

  val protoVersions = Set(1)
  val apiMajorVersions = Set(1)

  def parse(maxBufferSize: Int) = new StatefulStage[ByteString, MTTransport] {
    sealed trait ParserState
    case object HandshakeState extends ParserState
    case object AwaitPackage extends ParserState
    case class AwaitBytes(length: Int) extends ParserState
    case class FailedState(msg: String) extends ParserState

    type ParserStateT = (ParserState, ByteString)

    private var parserState: ParserStateT = (HandshakeState, ByteString.empty)

    @inline
    private def failedState(msg: String) = ((FailedState(msg), ByteString.empty), Vector.empty)

    def initial = new State {
      override def onPush(chunk: ByteString, ctx: Context[MTTransport]): Directive = {
        val (newState, res) = doParse(parserState._1, parserState._2 ++ chunk)(Vector.empty)
        newState._1 match {
          case FailedState(msg) =>
            // ctx.fail(new IllegalStateException(msg))
            ctx.pushAndFinish(ProtoPackage(Drop(0, msg)))
          case _ =>
            parserState = newState
            emit(res.iterator, ctx)
        }
      }

      // @tailrec
      private def doParse(state: ParserState, bs: ByteString)
                         (pSeq: Vector[MTTransport]): (ParserStateT, Vector[MTTransport]) = {
        @inline
        def parsePackage(bs: ByteString): (ParserStateT, Vector[MTTransport]) = {
          TransportPackageCodec.decode(bs)
          ???
        }

        if (bs.isEmpty) ((state, bs), pSeq)
        else state match {
          case HandshakeState => HandshakeCodec.decode(bs) match {
            case \/-(h) => ((AwaitPackage, ByteString.empty), Vector(h))
            case -\/(e) => failedState(e)
          }
          case AwaitPackage =>
            if (bs.length < int32Bytes) ((AwaitPackage, bs), pSeq)
            else {
              val pkgLength = CodecUtils.readInt32(bs).get
              val tail = bs.drop(int32Bytes)
              if (pkgLength < bs.length) ((AwaitBytes(pkgLength), tail), pSeq)
              else parsePackage(tail)
            }
          case AwaitBytes(awaitLength) =>
            if (awaitLength > bs.length) ((AwaitBytes(awaitLength), bs), pSeq)
            else parsePackage(bs)
        }
      }
    }
  }

  def mapResponse() = new PushPullStage[MTTransport, ByteString] {
    override def onPush(elem: MTTransport, ctx: Context[ByteString]): Directive = elem match {
      case h: Handshake =>
        if (h.protoVersion == 0 || h.apiMajorVersion == 0) ctx.pushAndFinish(ByteString(s"not ok: $h"))
        ctx.push(ByteString("ok"))
      case _ => ???
//      case MTPackage(m) => ctx.push(m)
//      case _: InternalError => ctx.pushAndFinish(ByteString.empty) // TODO: convert to BS
     case SilentClose => ctx.finish()
    }

    override def onPull(ctx: Context[ByteString]): Directive = ctx.pull()
  }

  def handlePackage(req: MTTransport, actorRef: ActorRef)
                   (implicit system: ActorSystem, timeout: Timeout): Future[MTTransport] = {
    import system.dispatcher
  
    req match {
      case ProtoPackage(MTPackage(p)) =>
        actorRef.ask(p).mapTo[MTTransport].recover {
          case e: AskTimeoutException =>
            val msg = s"handleAsk within $timeout"
            system.log.error(e, msg)
            ProtoPackage(InternalError(0, 0, msg))
        }
      case h: Handshake => Future.successful {
        val sha1Digest = MessageDigest.getInstance("SHA1")
        val clientBytes = ByteString(h.protoVersion, h.apiMajorVersion, h.apiMinorVersion) ++ h.randomBytes
        val sha1Sign = ByteString(sha1Digest.digest(clientBytes.toArray))
        val protoVersion: Byte = if (protoVersions.contains(h.protoVersion)) h.protoVersion else 0
        val apiMajorVersion: Byte = if (apiMajorVersions.contains(h.apiMajorVersion)) h.apiMajorVersion else 0
        val apiMinorVersion: Byte = if (apiMajorVersion == 0) 0 else h.apiMinorVersion
        Handshake(protoVersion, apiMajorVersion, apiMinorVersion, sha1Sign)         
      }
    }
  }
}
