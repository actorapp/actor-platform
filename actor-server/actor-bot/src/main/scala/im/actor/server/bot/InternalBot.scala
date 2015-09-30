package im.actor.server.bot

import akka.actor._
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import im.actor.api.rpc.messaging.ApiTextMessage
import im.actor.api.rpc.peers.{ ApiPeerType, ApiPeer }
import im.actor.bot.BotMessages
import im.actor.server.dialog.DialogExtension

import scala.concurrent.Future
import scala.concurrent.forkjoin.ThreadLocalRandom

private object InternalBot {

  final case class Initialized(authId: Long)
}

abstract class InternalBot(userId: Int, nickname: String, name: String) extends Actor with ActorLogging {

  import BotMessages._
  import InternalBot._

  private implicit val mat = ActorMaterializer()
  import context._

  protected val botExt = BotExtension(context.system)
  protected val dialogExt = DialogExtension(context.system)

  init()

  def onTextMessage(tm: TextMessage): Unit

  protected def sendTextMessage(peer: OutPeer, text: String): Unit = {
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
    val existence = botExt.exists(userId) flatMap { exists ⇒
      if (exists) {
        Future.successful(())
      } else {
        botExt.create(userId, nickname, name) map (_ ⇒ ())
      }
    }

    (for {
      _ ← existence
      authId ← botExt.getAuthId(userId)
    } yield Initialized(authId)) pipeTo self
  }
}
