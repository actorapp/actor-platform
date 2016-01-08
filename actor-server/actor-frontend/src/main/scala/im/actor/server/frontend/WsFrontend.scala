package im.actor.server.frontend

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.ServerSettings
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{ BinaryMessage, Message }
import akka.http.scaladsl.server.{ Directives, Route }
import akka.stream.Materializer
import akka.stream.scaladsl._
import akka.stream.stage.{ Context, PushStage, SyncDirective, TerminationDirective }
import akka.util.ByteString
import slick.driver.PostgresDriver.api.Database

import im.actor.server.session.SessionRegion
import im.actor.tls.TlsContext

import scala.concurrent.duration._

object WsFrontend extends Frontend("ws") {

  import Directives._

  val IdleTimeout = 15.minutes

  def start(host: String, port: Int, serverKeys: Seq[ServerKey], tlsContext: Option[TlsContext])(
    implicit
    sessionRegion: SessionRegion,
    db:            Database,
    system:        ActorSystem,
    mat:           Materializer
  ): Unit = {
    val log = Logging.getLogger(system, this)
    val defaultSettings = ServerSettings(system)

    val connections = Http().bind(
      host,
      port,
      httpsContext = tlsContext map (_.asHttpsContext),
      settings = defaultSettings.copy(timeouts = defaultSettings.timeouts.copy(idleTimeout = IdleTimeout))
    )

    connections runForeach { conn ⇒
      log.debug("New HTTP Connection {}", conn.remoteAddress)

      conn.handleWith(route(mtProtoBlueprint(serverKeys)))
    }
  }

  def route(flow: Flow[ByteString, ByteString, Unit])(
    implicit
    db:     Database,
    system: ActorSystem,
    mat:    Materializer
  ): Route = {
    get {
      pathSingleSlash {
        handleWebsocketMessages(websocket(flow))
      }
    }
  }

  def websocket(mtProtoFlow: Flow[ByteString, ByteString, Unit])(
    implicit
    system: ActorSystem,
    mat:    Materializer
  ): Flow[Message, Message, Unit] = {
    Flow[Message]
      .collect {
        case msg: BinaryMessage ⇒ msg
      }
      .flatMapConcat(_.dataStream)
      .via(mtProtoFlow)
      .map {
        case bs ⇒
          //system.log.debug("WS Send {}", BitVector(bs.toByteBuffer).toHex)
          BinaryMessage.Strict(bs)
      }
      .via(completionFlow(System.currentTimeMillis()))
  }

  def completionFlow[T](connStartTime: Long)(implicit system: ActorSystem): Flow[T, T, Unit] =
    Flow[T]
      .transform(() ⇒ new PushStage[T, T] {
        def onPush(elem: T, ctx: Context[T]): SyncDirective = ctx.push(elem)

        override def onUpstreamFailure(cause: Throwable, ctx: Context[T]): TerminationDirective = {
          system.log.error(s"WS stream failed with $cause")
          super.onUpstreamFailure(cause, ctx)
        }
      })
}