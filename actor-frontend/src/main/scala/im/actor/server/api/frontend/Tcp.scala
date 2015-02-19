package im.actor.server.api.frontend

import akka.actor._
import akka.util.Timeout
import akka.event.Logging
import akka.stream.scaladsl._
import akka.stream.FlowMaterializer
import im.actor.server.service.MTProto
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
      conn.handleWith(MTProto.flow(maxBufferSize))
    }

    val materializedServer = binding.connections.to(handler).run()

    binding.localAddress(materializedServer).onComplete {
      case Success(address) =>
        log.debug(s"Server started, listening on: $address")
      case Failure(e) =>
        log.error(e, "Server could not bind to serverAddress")
        system.shutdown()
    }
  }
}
