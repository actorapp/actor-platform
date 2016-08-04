package im.actor.server.frontend

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{ BinaryMessage, Message }
import akka.http.scaladsl.server.{ Directives, Route }
import akka.http.scaladsl.settings.ServerSettings
import akka.stream.{ ActorMaterializer, Materializer }
import akka.stream.scaladsl._
import akka.stream.stage.{ Context, PushStage, SyncDirective, TerminationDirective }
import akka.util.ByteString
import com.github.ghik.silencer.silent
import im.actor.server.session.SessionRegion

import scala.concurrent.duration._

object WsFrontend extends Frontend("ws") {

  import Directives._

  val IdleTimeout = 15.minutes

  def start(host: String, port: Int, serverKeys: Seq[ServerKey])(
    implicit
    sessionRegion: SessionRegion,
    system:        ActorSystem
  ): Unit = {
    val log = Logging.getLogger(system, this)
    val defaultSettings = ServerSettings(system)
    implicit val materializer: Materializer = ActorMaterializer()

    val connections = Http().bind(
      host,
      port,
      connectionContext = Http().defaultServerHttpContext,
      settings = defaultSettings.withTimeouts(defaultSettings.timeouts.withIdleTimeout(IdleTimeout))
    )

    connections runForeach { conn ⇒
      log.debug("New HTTP Connection {}", conn.remoteAddress)

      conn.handleWith(route(mtProtoBlueprint(serverKeys, conn.remoteAddress.getAddress())))
    }
  }

  def route(flow: Flow[ByteString, ByteString, akka.NotUsed])(implicit system: ActorSystem): Route = {
    get {
      pathSingleSlash {
        handleWebSocketMessages(websocket(flow))
      }
    }
  }

  def websocket(mtProtoFlow: Flow[ByteString, ByteString, akka.NotUsed])(implicit system: ActorSystem): Flow[Message, Message, akka.NotUsed] = {
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

  @silent
  def completionFlow[T](connStartTime: Long)(implicit system: ActorSystem): Flow[T, T, akka.NotUsed] =
    Flow[T]
      .transform(() ⇒ new PushStage[T, T] {
        def onPush(elem: T, ctx: Context[T]): SyncDirective = ctx.push(elem)

        override def onUpstreamFailure(cause: Throwable, ctx: Context[T]): TerminationDirective = {
          system.log.error(s"WS stream failed with $cause")
          super.onUpstreamFailure(cause, ctx)
        }
      })
}
