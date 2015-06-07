package im.actor.server.api.frontend

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

import akka.actor._
import akka.event.Logging
import akka.stream.FlowMaterializer
import akka.stream.scaladsl._
import akka.util.Timeout
import com.typesafe.config.Config
import slick.driver.PostgresDriver.api.Database

import im.actor.server.session.SessionRegion

object TcpFrontend extends Frontend {
  val connCounter = new AtomicLong(0L)

  def start(appConf: Config, sessionRegion: SessionRegion)(implicit db: Database, system: ActorSystem, materializer: FlowMaterializer): Unit = {
    val log = Logging.getLogger(system, this)
    val config = appConf.getConfig("frontend.tcp")

    implicit val askTimeout = Timeout(config.getDuration("timeout", TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS)
    val maxBufferSize = config.getBytes("max-buffer-size").toInt
    val interface = config.getString("interface")
    val port = config.getInt("port")

    val connections = Tcp().bind(interface, port)

    connections runForeach { conn ⇒
      log.info(s"Client connected from: ${conn.remoteAddress}")

      try {
        val flow = MTProto.flow(nextConnId(), maxBufferSize, sessionRegion)
        conn.handleWith(flow)
      } catch {
        case e: Exception ⇒
          log.error(e, "Failed to create connection flow")
      }
    }
  }
}
