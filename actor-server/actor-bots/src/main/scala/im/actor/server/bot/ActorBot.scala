package im.actor.server.bot

import akka.actor.{ Props, ActorSystem }
import im.actor.api.rpc.peers.{ ApiPeerType, ApiPeer }
import im.actor.bots.BotMessages
import im.actor.server.user.UserErrors
import im.actor.util.misc.IdUtils

import scala.util.{ Failure, Success }

object ActorBot {
  val UserId = 10
  val Username = "actor"
  val Name = "Actor Bot"

  val NewCmd = "/bot new"

  val ApiPeer = new ApiPeer(ApiPeerType.Private, UserId)

  def start()(implicit system: ActorSystem) = InternalBot.start(props)

  private def props = Props(classOf[ActorBot])
}

final class ActorBot extends InternalBot(ActorBot.UserId, ActorBot.Username, ActorBot.Name, isAdmin = true) {
  import BotMessages._
  import ActorBot._

  import context._

  override def onMessage(m: Message): Unit = {
    m.message match {
      case TextMessage(text, ext) ⇒
        if (m.peer.isPrivate && text.startsWith(NewCmd)) {
          text.drop(NewCmd.length + 1).trim.split(" ").map(_.trim).toList match {
            case nickname :: name :: Nil ⇒
              log.warning("Creating new bot")

              requestCreateBot(nickname, name) onComplete {
                case Success(token) ⇒ requestSendMessage(m.peer, nextRandomId(), TextMessage(s"Yay! Bot created, bot token: ${token.token}, bot id: ${token.userId}", None))
                case Failure(BotError(_, "USERNAME_TAKEN", _, _)) ⇒
                  requestSendMessage(m.peer, nextRandomId(), TextMessage("Username already taken", None))
                case Failure(e) ⇒
                  log.error(e, "Failed to create bot")
                  requestSendMessage(m.peer, nextRandomId(), TextMessage("There was a problem on our side. Please, try again a bit later.", None))
              }
            case _ ⇒ requestSendMessage(m.peer, nextRandomId(), TextMessage("Command format is: /bot new <nickname> <name>", None))
          }
        }
      case _ ⇒
    }
  }

  override def onRawUpdate(u: RawUpdate): Unit = {}
}