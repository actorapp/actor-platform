package im.actor.server.api.frontend

import java.util.concurrent.TimeUnit

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

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

object WsFrontend {
  import Directives._

  def start(appConf: Config, sessionRegion: SessionRegion)(implicit db: Database, system: ActorSystem, materializer: FlowMaterializer): Unit = {
    val config = appConf.getConfig("frontend.ws")

    implicit val askTimeout = Timeout(config.getDuration("timeout", TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS)
    implicit val ec: ExecutionContext = system.dispatcher

    val maxBufferSize = config.getBytes("max-buffer-size").toInt
    val interface = config.getString("interface")
    val port = config.getInt("port")

    val binding = Http().bindAndHandle(route(maxBufferSize, sessionRegion), interface, port)

    binding onComplete {
      case Success(binding) ⇒
        val localAddress = binding.localAddress
        system.log.info(s"WebSocket Frontend is listening on ${localAddress.getHostName}:${localAddress.getPort}")
      case Failure(e) ⇒
        system.log.error(s"Binding failed with ${e.getMessage}")
        system.shutdown()
    }
  }

  def route(maxBufferSize: Int, sessionRegion: SessionRegion)(implicit db: Database, timeout: Timeout, system: ActorSystem): Route = {
    get {
      pathSingleSlash {
        val mtProtoFlow = MTProto.flow(maxBufferSize, sessionRegion)

        handleWebsocketMessages(flow(mtProtoFlow))
      }
    }
  }

  def flow(mtProtoFlow: Flow[ByteString, ByteString, Unit])(implicit system: ActorSystem): Flow[Message, Message, Unit] = {
    Flow[Message]
      .collect {
        case BinaryMessage.Strict(msg) ⇒
          system.log.debug("WS Strict {}", msg)
          msg
      }
      .via(mtProtoFlow)
      .map {
        case bs ⇒ BinaryMessage.Strict(bs)
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