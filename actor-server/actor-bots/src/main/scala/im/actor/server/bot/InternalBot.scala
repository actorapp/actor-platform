package im.actor.server.bot

import akka.actor._
import akka.pattern.pipe
import akka.stream.scaladsl.{ Sink, Source }
import akka.stream.{ ActorMaterializer, OverflowStrategy }
import akka.util.Timeout
import im.actor.botkit.BotBase
import im.actor.config.ActorConfig
import im.actor.server.dialog.DialogExtension

import scala.concurrent.Future
import scala.util.Failure

private object InternalBot {

  final case class Initialized(authId: Long)

}

abstract class InternalBot(userId: Int, nickname: String, name: String, isAdmin: Boolean) extends BotBase {

  import InternalBot._

  private implicit val mat = ActorMaterializer()

  import context._

  override protected implicit val timeout = Timeout(ActorConfig.defaultTimeout)

  protected val botExt = BotExtension(context.system)
  protected val dialogExt = DialogExtension(context.system)

  init()

  def receive = {
    case Initialized(authId) ⇒
      val bp = new BotServerBlueprint(userId, authId, system)

      val rqSource =
        Source.actorRef(100, OverflowStrategy.fail)
          .via(bp.flow)
          .to(Sink.actorRef(self, Kill))
          .run()

      setRqSource(rqSource)

      context become workingBehavior
  }

  private def init() = {
    log.warning("Initiating bot {} {} {}", userId, nickname, name)

    val existence = botExt.exists(userId) flatMap { exists ⇒
      if (exists) {
        log.warning("Bot already exists")
        Future.successful(())
      } else {
        log.warning("Creating user {}", userId)
        botExt.create(userId, nickname, name, isAdmin) map (_ ⇒ ()) andThen {
          case Failure(e) ⇒ log.error(e, "Failed to create bot user")
        }
      }
    }

    (for {
      _ ← existence
      authId ← botExt.getAuthId(userId)
    } yield Initialized(authId)) pipeTo self
  }

  override protected def onStreamFailure(cause: Throwable): Unit = {
    log.error(cause, "Bot stream failure")
    throw cause
  }
}
