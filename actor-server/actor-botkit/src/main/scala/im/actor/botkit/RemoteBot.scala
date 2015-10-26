package im.actor.botkit

import java.net.URLEncoder

import akka.actor._
import akka.http.scaladsl.Http
import akka.stream.scaladsl.{ Sink, Source }
import akka.stream.{ ActorMaterializer, OverflowStrategy }
import akka.util.Timeout
import im.actor.bots.BotMessages
import im.actor.bots.BotMessages.ResponseBody
import im.actor.concurrent.ActorFutures
import upickle.default._

import scala.concurrent.Promise
import scala.concurrent.duration._

object RemoteBot {
  val DefaultEndpoint = "wss://api.actor.im"

  private final object StreamComplete
}

abstract class RemoteBot(token: String, endpoint: String) extends BotBase with ActorFutures {

  import BotMessages._
  import RemoteBot._

  override protected implicit val timeout: Timeout = Timeout(30.seconds)
  private implicit val mat = ActorMaterializer()

  private var rqSource = initFlow()

  def onReceive(message: Object): Unit = {}

  def receive = internalReceive orElse {
    case message ⇒
      onReceive(message.asInstanceOf[Object])
  }

  override protected def onStreamFailure(cause: Throwable): Unit =
    log.error(cause, "Bot stream failure")

  private final def internalReceive: Receive = workingBehavior(rqSource).orElse({
    case StreamComplete ⇒
      log.warning("Disconnected, reinitiating flow")
      rqSource = initFlow()
  })

  private def initFlow(): ActorRef = {
    val (wsSource, wsSink) = WebsocketClient.sourceAndSink(s"$endpoint/v1/bots/${URLEncoder.encode(token, "UTF-8")}")

    wsSource.map(read[BotMessageOut]).to(Sink.actorRef(self, StreamComplete)).run()

    Source.actorRef(bufferSize = 100, overflowStrategy = OverflowStrategy.fail)
      .map(write[BotRequest])
      .to(wsSink)
      .run()
  }
}