package im.actor.botkit

import java.net.URLEncoder
import java.util.concurrent.ThreadLocalRandom

import akka.actor._
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import im.actor.bots.BotMessages.ResponseBody
import im.actor.bots.{BotMessageOut, BotMessages}
import im.actor.bots.macros.BotInterface
import im.actor.concurrent.ActorFutures
import upickle.default._

import scala.collection.concurrent.TrieMap
import scala.concurrent.{Future, Promise}

object RemoteBot {
  val DefaultEndpoint = "wss://api.actor.im"

  private final case class NextRequest(body: BotMessages.RequestBody, responsePromise: Promise[ResponseBody])

}

@BotInterface
private[botkit] abstract class RemoteBotBase extends Actor with ActorLogging with ActorFutures

abstract class RemoteBot(token: String, endpoint: String) extends RemoteBotBase {

  import BotMessages._
  import RemoteBot._
  import context.dispatcher

  private implicit val mat = ActorMaterializer()

  private var rqSource = initFlow()
  private var rqCounter: Long = 0
  private var requests = Map.empty[Long, Promise[ResponseBody]]
  private var users = TrieMap.empty[Int, User]
  private var groups = TrieMap.empty[Int, Group]

  def onReceive(message: Object): Unit = {}

  def receive = internalReceive orElse {
    case message ⇒
      onReceive(message.asInstanceOf[Object])
  }

  @throws[Exception](classOf[Exception])
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    val prefix = "Actor will restart."

    message match {
      case Some(msg) ⇒
        log.error(reason, prefix + " Last message received: {}", msg)
      case None ⇒
        log.error(reason, prefix)
    }

    super.preRestart(reason, message)
  }

  private final def internalReceive: Receive = {
    case ConnectionClosed ⇒
      log.warning("Disconnected, reinitiating flow")
      rqSource = initFlow()
    case Status.Failure(e) ⇒
      log.error(e, "Error in a stream, restarting")
      rqSource = initFlow()
    case rsp: BotResponse ⇒
      log.info("Response #{}: {}", rsp.id, rsp.body)
      requests.get(rsp.id) foreach (_.success(rsp.body))
    case upd: BotUpdate =>
      log.info("Update: {}", upd)

      upd match {
        case BotFatSeqUpdate(_, _, users, groups) =>
          users foreach {
            case (id, user) => this.users.putIfAbsent(id, user)
          }

          groups foreach {
            case (id, group) => this.groups.putIfAbsent(id, group)
          }
        case _ =>
      }

      onUpdate(upd.body)
    case NextRequest(body, responsePromise) ⇒
      log.info("Request #{}: {}", rqCounter, body)
      rqCounter += 1
      rqSource ! BotRequest(rqCounter, body)
      requests += (rqCounter → responsePromise)
    case unmatched ⇒
      log.error("Unmatched {}", unmatched)
  }

  override def request[T <: RequestBody](body: T): Future[body.Response] = {
    val promise = Promise[ResponseBody]()
    self ! NextRequest(body, promise)
    promise.future map (_.asInstanceOf[body.Response])
  }

  protected def getUser(id: Int) = this.users.get(id).getOrElse(throw new RuntimeException(s"User $id not found"))

  protected def getGroup(id: Int) = this.groups.get(id).getOrElse(throw new RuntimeException(s"Group $id not found"))

  protected def nextRandomId(): Long = ThreadLocalRandom.current().nextLong()

  private def initFlow(): ActorRef = {
    val (wsSource, wsSink) = WebsocketClient.sourceAndSink(s"${endpoint}/v1/bots/${URLEncoder.encode(token, "UTF-8")}")

    wsSource.map(read[BotMessageOut]).to(Sink.actorRef(self, ConnectionClosed)).run()

    Source.actorRef(bufferSize = 100, overflowStrategy = OverflowStrategy.fail)
      .map(write[BotRequest])
      .map{r => println(r);r}
      .to(wsSink)
      .run()
  }
}