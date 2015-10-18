package im.actor.server.bot

import akka.actor.{ Props, ActorSystem }
import im.actor.api.rpc.peers.{ ApiPeerType, ApiPeer }
import im.actor.bots.BotMessages
import im.actor.server.user.UserExceptions
import im.actor.util.misc.IdUtils

import scala.util.{ Failure, Success }

object ActorBot {
  val UserId = 10
  val Username = "actor"
  val Name = "Actor Bot"

  val NewCmd = "/bot new"

  val ApiPeer = new ApiPeer(ApiPeerType.Private, UserId)

  def start()(implicit system: ActorSystem) = system.actorOf(props, "ActorBot")

  private def props = Props(classOf[ActorBot])
}

final class ActorBot extends InternalBot(ActorBot.UserId, ActorBot.Username, ActorBot.Name, isAdmin = true) {
  import BotMessages._
  import ActorBot._

  import context._

  override def onTextMessage(tm: TextMessage): Unit = {
    if (tm.peer.isPrivate && tm.text.startsWith(NewCmd)) {
      tm.text.drop(NewCmd.length + 1).trim.split(" ").map(_.trim).toList match {
        case nickname :: name :: Nil ⇒
          log.warning("Creating new bot")

          requestCreateBot(nickname, name) onComplete {
            case Success(token) ⇒ requestSendTextMessage(tm.peer, nextRandomId(), s"Yay! Bot created, here is your token: ${token}")
            case Failure(BotError(_, "USERNAME_TAKEN", _, _)) ⇒
              requestSendTextMessage(tm.peer, nextRandomId(), "Username already taken")
            case Failure(e) ⇒
              log.error(e, "Failed to create bot")
              requestSendTextMessage(tm.peer, nextRandomId(), "There was a problem on our side. Please, try again a bit later.")
          }
        case _ ⇒ requestSendTextMessage(tm.peer, nextRandomId(), "Command format is: /bot new <nickname> <name>")
      }
    }
  }
}