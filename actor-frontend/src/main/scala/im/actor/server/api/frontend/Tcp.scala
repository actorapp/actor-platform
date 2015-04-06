package im.actor.server.api.frontend

import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

import scala.util.Try

import akka.actor._
import akka.event.Logging
import akka.stream.FlowMaterializer
import akka.stream.scaladsl._
import akka.util.Timeout
import com.typesafe.config.Config
import slick.driver.PostgresDriver.api.Database

import im.actor.server.session.Session

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

    val sessionRegion = Session.startRegionProxy()

    connections runForeach { conn =>
      log.info(s"Client connected from: ${conn.remoteAddress}")

      try {
        val flow = MTProto.flow(maxBufferSize, sessionRegion)
        conn.handleWith(flow)
      } catch {
        case e: Exception =>
          log.error(e, "Failed to create connection flow")
      }
    }
  }
}
