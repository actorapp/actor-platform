package im.actor.server.bot

import akka.actor._
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import im.actor.api.rpc.messaging.ApiTextMessage
import im.actor.api.rpc.peers.{ ApiPeerType, ApiPeer }
import im.actor.bots.BotMessages
import im.actor.bots.macros.BotInterface
import im.actor.server.dialog.DialogExtension
import im.actor.server.sequence.SeqStateDate

import scala.concurrent.Future
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.util.Failure

private object InternalBot {

  final case class Initialized(authId: Long)
}

@BotInterface
private[bot] abstract class InternalBotBase extends Actor with ActorLogging

abstract class InternalBot(userId: Int, nickname: String, name: String) extends InternalBotBase {

  import InternalBot._
  import BotMessages._

  private implicit val mat = ActorMaterializer()
  import context._

  protected val botExt = BotExtension(context.system)
  protected val dialogExt = DialogExtension(context.system)

  init()

  override def request[T <: RequestBody](body: T): Future[body.Response] = body match {
    case SendTextMessage(peer, randomId, text) ⇒
      for {
        SeqStateDate(_, _, date) ← dialogExt.sendMessage(
          peer = ApiPeer(ApiPeerType(peer.`type`), peer.id),
          senderUserId = userId,
          randomId = randomId,
          senderAuthId = 0,
          message = ApiTextMessage(text, Vector.empty, None),
          isFat = false
        )
      } yield MessageSent(date).asInstanceOf[body.Response]
  }

  def receive = {
    case Initialized(authId) ⇒
      val flowRef = UpdatesSource.source(authId)
        .to(Sink.actorRef(self, PoisonPill))
        .run()

      context become working(flowRef)
  }

  private final def working(flowRef: ActorRef): Receive = {
    case upd: BotUpdate ⇒
      log.debug("Update {}", upd)
      onUpdate(upd.body)
    case unmatched ⇒
      log.warning("Unmatched {}", unmatched)
  }

  //override def onTextMessage(tm: TextMessage): Unit = {}

  private def init() = {
    log.warning("Initiating bot {} {} {}", userId, nickname, name)

    val existence = botExt.exists(userId) flatMap { exists ⇒
      if (exists) {
        log.warning("Bot already exists")
        Future.successful(())
      } else {
        log.warning("Creating user {}", userId)
        botExt.create(userId, nickname, name) map (_ ⇒ ()) andThen {
          case Failure(e) ⇒ log.error(e, "Failed to create bot user")
        }
      }
    }

    (for {
      _ ← existence
      authId ← botExt.getAuthId(userId)
    } yield Initialized(authId)) pipeTo self
  }

  protected def nextRandomId() = ThreadLocalRandom.current().nextLong()
}
