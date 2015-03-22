package im.actor.server.api.frontend

import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

import akka.actor._
import akka.event.Logging
import akka.stream.FlowMaterializer
import akka.stream.scaladsl._
import akka.util.Timeout
import com.typesafe.config.Config
import slick.driver.PostgresDriver.api.Database

import im.actor.server.api.service.MTProto

object Tcp {
  def start(appConf: Config)(implicit db: Database, system: ActorSystem, materializer: FlowMaterializer): Unit = {
    val log = Logging.getLogger(system, this)
    val config = appConf.getConfig("frontend.tcp")

    implicit val askTimeout = Timeout(config.getDuration("timeout", TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS)
    val maxBufferSize = config.getBytes("max-buffer-size").toInt
    val interface = config.getString("interface")
    val port = config.getInt("port")
    val serverAddress = new InetSocketAddress(interface, port)

    val connections = StreamTcp().bind(serverAddress)

    connections runForeach { conn =>
      log.info(s"Client connected from: ${conn.remoteAddress}")
      val flow = MTProto.flow(maxBufferSize)
      conn.handleWith(flow)
    }
  }
}
