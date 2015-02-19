package im.actor.server.api.frontend

import akka.actor.ActorSystem
import akka.event.Logging
import akka.pattern.ask
import akka.stream.FlowMaterializer
import akka.stream.scaladsl._
import akka.util.{ ByteString, Timeout }
import com.typesafe.config.Config
import im.actor.server.service.MTProto
import spray.can.websocket.frame._
import streamwebsocket._
import java.util.concurrent.TimeUnit

object Ws {
  def start(appConf: Config)(implicit system: ActorSystem, materializer: FlowMaterializer): Unit = {
    import system.dispatcher
    import WebSocketMessage._

//    val log = Logging.getLogger(system, this)
    val config = appConf.getConfig("frontend.ws")
    implicit val askTimeout = Timeout(config.getDuration("timeout", TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS)
    val maxBufferSize = config.getBytes("max-buffer-size").toInt
    val interface = config.getString("interface")
    val port = config.getInt("port")

    val flow = Flow[Frame, Frame]() { implicit b =>
      import FlowGraphImplicits._

      val in = UndefinedSource[Frame]
      val out = UndefinedSink[Frame]
      val toBs = Flow[Frame].map { case BinaryFrame(bs) => bs }
      val fromBs = Flow[ByteString].map(BinaryFrame(_))

      in ~> toBs ~> MTProto.flow(maxBufferSize) ~> fromBs ~> out

      (in, out)
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
