package im.actor.server.api.frontend

import akka.actor._
import akka.stream.actor.ActorPublisher
import akka.util.{ ByteString, Timeout }
import akka.event.Logging
import akka.stream.scaladsl._
import akka.stream.FlowMaterializer
import im.actor.server.api.mtproto.transport.MTTransport
import im.actor.server.api.service._
import scala.util.{ Success, Failure }
import com.typesafe.config.Config
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

object Tcp {
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

    val handler = ForeachSink[StreamTcp.IncomingConnection] { conn =>
      log.info(s"Client connected from: ${conn.remoteAddress}")

      val actor = system.actorOf(AuthorizationActor.props())
      val (watchActor, watchSource) = SourceWatchActor[MTTransport](actor)
      val actorSource = Source(ActorPublisher[MTTransport](actor))

      val handleFlow = Flow[ByteString]
        .transform(() => MTProto.parse(maxBufferSize))
        .mapAsyncUnordered(MTProto.handlePackage(_, actor))
      val responseFlow = Flow[MTTransport]
        .transform(() => MTProto.mapResponse())
      val completeSink = Sink.onComplete {
        case _ =>
          watchActor ! PoisonPill
          actor ! PoisonPill
      }
      val flow = Flow[ByteString, ByteString]() { implicit b =>
        import FlowGraphImplicits._

        val in = UndefinedSource[ByteString]
        val out = UndefinedSink[ByteString]
        val bcast = Broadcast[ByteString]
        val merge = Merge[MTTransport]

        in ~> handleFlow ~> merge
        actorSource      ~> merge
        watchSource      ~> merge

        merge ~> responseFlow ~> bcast

        bcast ~> out
        bcast ~> completeSink

        (in, out)
      }
      conn.handleWith(flow)
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
