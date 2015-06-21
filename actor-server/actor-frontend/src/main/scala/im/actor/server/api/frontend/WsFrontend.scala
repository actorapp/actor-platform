package im.actor.server.api.frontend

import java.util.concurrent.TimeUnit

import scala.concurrent.ExecutionContext

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{ BinaryMessage, Message }
import akka.http.scaladsl.server.{ Directives, Route }
import akka.stream.FlowMaterializer
import akka.stream.scaladsl._
import akka.stream.stage.{ Context, PushStage, SyncDirective, TerminationDirective }
import akka.util.{ ByteString, Timeout }
import com.typesafe.config.Config
import slick.driver.PostgresDriver.api.Database

import im.actor.server.session.SessionRegion

object WsFrontend extends Frontend {

  import Directives._

  override protected val connIdPrefix = "ws"

  def start(appConf: Config, sessionRegion: SessionRegion)(implicit db: Database, system: ActorSystem, materializer: FlowMaterializer): Unit = {
    val config = appConf.getConfig("frontend.ws")

    implicit val askTimeout = Timeout(config.getDuration("timeout", TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS)
    implicit val ec: ExecutionContext = system.dispatcher

    val interface = config.getString("interface")
    val port = config.getInt("port")

    val connections = Http().bind(interface, port)

    connections runForeach { conn ⇒
      conn.handleWith(route(sessionRegion))
    }
  }

  def route(sessionRegion: SessionRegion)(implicit db: Database, timeout: Timeout, system: ActorSystem): Route = {
    get {
      pathSingleSlash {
        val mtProtoFlow = MTProto.flow(nextConnId(), sessionRegion)

        handleWebsocketMessages(flow(mtProtoFlow))
      }
    }
  }

  def flow(mtProtoFlow: Flow[ByteString, ByteString, Unit])(implicit system: ActorSystem): Flow[Message, Message, Unit] = {
    Flow[Message]
      .collect {
        case BinaryMessage.Strict(msg) ⇒
          //system.log.debug("WS Receive {}", BitVector(msg.toByteBuffer).toHex)
          msg
      }
      .via(mtProtoFlow)
      .map {
        case bs ⇒
          //system.log.debug("WS Send {}", BitVector(bs.toByteBuffer).toHex)
          BinaryMessage.Strict(bs)
      }
      .via(reportErrorsFlow)
  }

  def reportErrorsFlow[T](implicit system: ActorSystem): Flow[T, T, Unit] =
    Flow[T]
      .transform(() ⇒ new PushStage[T, T] {
        def onPush(elem: T, ctx: Context[T]): SyncDirective = ctx.push(elem)

        override def onUpstreamFailure(cause: Throwable, ctx: Context[T]): TerminationDirective = {
          system.log.error(s"WS stream failed with $cause")
          super.onUpstreamFailure(cause, ctx)
        }
      })
}