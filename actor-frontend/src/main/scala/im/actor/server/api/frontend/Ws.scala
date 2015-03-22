package im.actor.server.api.frontend

import akka.actor.ActorSystem
import akka.event.Logging
import akka.pattern.ask
import akka.stream.FlowMaterializer
import akka.stream.scaladsl._
import akka.util.{ ByteString, Timeout }
import com.typesafe.config.Config
import im.actor.server.api.service.MTProto
import slick.driver.PostgresDriver.api.Database
import spray.can.websocket.frame._
import streamwebsocket._
import java.util.concurrent.TimeUnit

object Ws {
  def start(appConf: Config)(implicit db: Database, system: ActorSystem, materializer: FlowMaterializer): Unit = {
    import system.dispatcher
    import WebSocketMessage._

//    val log = Logging.getLogger(system, this)
    val config = appConf.getConfig("frontend.ws")
    implicit val askTimeout = Timeout(config.getDuration("timeout", TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS)
    val maxBufferSize = config.getBytes("max-buffer-size").toInt
    val interface = config.getString("interface")
    val port = config.getInt("port")

    val flow = Flow() { implicit builder =>
      import FlowGraph.Implicits._

      val toBs = builder.add(Flow[Frame].map { case BinaryFrame(bs) => bs })
      val fromBs = builder.add(Flow[ByteString].map(BinaryFrame(_)))

      toBs ~> MTProto.flow(maxBufferSize) ~> fromBs

      (toBs.inlet, fromBs.outlet)
    }

    val server = system.actorOf(WebSocketServer.props(), "frontend-ws")
    (server ? WebSocketMessage.Bind(interface, port)).map {
      case Bound(_, connections) =>
        Source(connections).runForeach {
          case WebSocketMessage.Connection(inbound, outbound) =>
            flow.runWith(Source(inbound), Sink(outbound))
        }
    }
  }
}
