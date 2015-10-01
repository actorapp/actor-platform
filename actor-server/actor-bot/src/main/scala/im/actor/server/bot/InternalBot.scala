package im.actor.server.bot

import akka.actor._
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import im.actor.api.rpc.messaging.ApiTextMessage
import im.actor.api.rpc.peers.{ ApiPeerType, ApiPeer }
import im.actor.bot.{ BotBase, BotMessages }
import im.actor.server.dialog.DialogExtension

import scala.concurrent.Future
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.util.Failure

private object InternalBot {

  final case class Initialized(authId: Long)
}

abstract class InternalBot(userId: Int, nickname: String, name: String) extends Actor with ActorLogging with BotBase {

  import BotMessages._
  import InternalBot._

  private implicit val mat = ActorMaterializer()
  import context._

  protected val botExt = BotExtension(context.system)
  protected val dialogExt = DialogExtension(context.system)

  init()

  override protected def sendTextMessage(peer: OutPeer, text: String): Unit = {
    // FIXME: check access hash
    dialogExt.sendMessage(
      peer = ApiPeer(ApiPeerType(peer.`type`), peer.id),
      senderUserId = userId,
      randomId = ThreadLocalRandom.current().nextLong(),
      senderAuthId = 0,
      message = ApiTextMessage(text, Vector.empty, None),
      isFat = false
    )
  }

  def receive = {
    case Initialized(authId) ⇒
      val flowRef = UpdatesSource.source(authId)
        .to(Sink.actorRef(self, PoisonPill))
        .run()

      context become working(flowRef)
  }

  private final def working(flowRef: ActorRef): Receive = {
    case tm: TextMessage ⇒
      log.debug("TextMessage {}", tm)

      onTextMessage(tm)
    case unmatched ⇒
      log.warning("Unmatched {}", unmatched)
  }

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
}
