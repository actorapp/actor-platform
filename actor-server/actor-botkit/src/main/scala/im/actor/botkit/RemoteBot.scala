package im.actor.botkit

import java.net.URLEncoder

import akka.actor._
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import im.actor.bot.{BotBase, BotMessageOut, BotMessages}
import upickle.default._

import scala.concurrent.forkjoin.ThreadLocalRandom

object RemoteBot {
  val DefaultEndpoint = "wss://api.actor.im"
  private final case class NextRequest(body: BotMessages.RequestBody)
}

abstract class RemoteBot(token: String, endpoint: String) extends BotBase with Actor with ActorLogging {

  import RemoteBot._
  import BotMessages._

  private implicit val mat = ActorMaterializer()

  private var rqSource = initFlow()
  private var rqCounter: Long = 0

  def receive: Receive = {
    case ConnectionClosed ⇒
      log.warning("Disconnected, reinitiating flow")
      rqSource = initFlow()
    case Status.Failure(e) ⇒
      log.error(e, "Error in a stream, restarting")
      rqSource = initFlow()
    case tm: TextMessage ⇒
      log.info("Received text message {}", tm)
      onTextMessage(tm)
    case rsp: BotResponse ⇒
      log.info("Response: {}", rsp.body)
    case unmatched ⇒
      log.error("Unmatched {}", unmatched)
    case NextRequest(body) =>
      rqSource ! nextRequest(body)
  }

  override protected def sendTextMessage(peer: OutPeer, text: String): Unit = {
    log.info("Sending message, peer: {}, text: {}", peer, text)
    log.info("rqSource {}", rqSource)
    self ! NextRequest(SendTextMessage(peer, ThreadLocalRandom.current().nextLong(), text))
  }

  private def nextRequest(body: RequestBody): BotRequest = {
    rqCounter += 1
    BotRequest(rqCounter, body)
  }

  private def initFlow(): ActorRef = {
    val (wsSource, wsSink) = WebsocketClient.sourceAndSink(s"${endpoint}/v1/bots/${URLEncoder.encode(token, "UTF-8")}")

    wsSource.map(read[BotMessageOut]).to(Sink.actorRef(self, ConnectionClosed)).run()

    Source.actorRef(bufferSize = 100, overflowStrategy = OverflowStrategy.fail)
      .map(write[BotRequest])
      .to(wsSink)
      .run()
  }
}