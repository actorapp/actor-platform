package im.actor.botkit

import java.net.URLEncoder

import akka.stream.scaladsl.{ Sink, Source }
import akka.stream.{ ActorMaterializer, OverflowStrategy }
import akka.util.Timeout
import im.actor.bots.BotMessages
import im.actor.concurrent.ActorFutures
import upickle.default._

import scala.concurrent.duration._

object RemoteBot {
  val DefaultEndpoint = "wss://api.actor.im"

  private object StreamComplete
}

abstract class RemoteBot(token: String, endpoint: String) extends BotBase with ActorFutures {

  import BotMessages._
  import RemoteBot._

  override protected implicit val timeout: Timeout = Timeout(30.seconds)
  private implicit val mat = ActorMaterializer()

  initFlow()

  def onReceive(message: Object): Unit = {}

  def receive: Receive = internalReceive orElse {
    case message ⇒
      onReceive(message.asInstanceOf[Object])
  }

  override protected def onStreamFailure(cause: Throwable): Unit = {
    log.error(cause, "Stream failure")
    initFlow()
  }

  private final def internalReceive: Receive = workingBehavior.orElse({
    case StreamComplete ⇒
      log.warning("Disconnected, reinitiating flow")
      initFlow()
  })

  private def initFlow(): Unit = {
    val (wsSource, wsSink) = WebsocketClient.sourceAndSink(s"$endpoint/v1/bots/${URLEncoder.encode(token, "UTF-8")}")

    wsSource.map(read[BotMessageOut]).to(Sink.actorRef(self, StreamComplete)).run()

    val rqSource = Source.actorRef(bufferSize = 100, overflowStrategy = OverflowStrategy.fail)
      .map(write[BotRequest])
      .to(wsSink)
      .run()

    setRqSource(rqSource)
  }
}