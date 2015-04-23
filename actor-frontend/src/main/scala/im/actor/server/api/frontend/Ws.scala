package im.actor.server.api.frontend

import java.util.concurrent.TimeUnit

import scala.concurrent.ExecutionContext

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.stream.FlowMaterializer
import akka.stream.scaladsl._
import akka.util.{ ByteString, Timeout }
import com.typesafe.config.Config
import slick.driver.PostgresDriver.api.Database
import spray.can.websocket.frame._
import streamwebsocket._

import im.actor.server.session.Session

object Ws {

  import WebSocketMessage._

  def start(appConf: Config)(implicit db: Database, system: ActorSystem, materializer: FlowMaterializer): Unit = {
    val config = appConf.getConfig("frontend.ws")

    implicit val askTimeout = Timeout(config.getDuration("timeout", TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS)
    implicit val ec: ExecutionContext = system.dispatcher

    val maxBufferSize = config.getBytes("max-buffer-size").toInt
    val interface = config.getString("interface")
    val port = config.getInt("port")

    val sessionRegion = Session.startRegionProxy()

    val flow = Flow() { implicit builder ⇒
      import FlowGraph.Implicits._

      val toBs = builder.add(Flow[Frame].map { case BinaryFrame(bs) ⇒ bs })
      val fromBs = builder.add(Flow[ByteString].map(BinaryFrame(_)))

      toBs ~> MTProto.flow(maxBufferSize, sessionRegion) ~> fromBs

      (toBs.inlet, fromBs.outlet)
    }

    val server = system.actorOf(WebSocketServer.props(), "frontend-ws")
    (server ? WebSocketMessage.Bind(interface, port)).map {
      case Bound(_, connections) ⇒
        Source(connections).runForeach {
          case WebSocketMessage.Connection(inbound, outbound) ⇒
            flow.runWith(Source(inbound), Sink(outbound))
        }
    }
  }
}
