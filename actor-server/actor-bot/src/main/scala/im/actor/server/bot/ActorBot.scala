package im.actor.server.bot

import akka.actor.{ Props, ActorSystem }
import im.actor.api.rpc.peers.{ ApiPeerType, ApiPeer }
import im.actor.bot.BotMessages
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

final class ActorBot extends InternalBot(ActorBot.UserId, ActorBot.Username, ActorBot.Name) {
  import BotMessages._
  import ActorBot._

  import context._

  override def onTextMessage(tm: TextMessage): Unit = {
    if (isPrivate(tm.peer) && tm.text.startsWith(NewCmd)) {
      tm.text.drop(NewCmd.length + 1).trim.split(" ").map(_.trim).toList match {
        case xs if xs.length == 2 ⇒
          val nickname = xs(0)
          val name = xs(1)

          log.warning("Creating new bot")

          val userId = IdUtils.nextIntId()

          botExt.create(userId, nickname, name) onComplete {
            case Success(token) ⇒
              sendTextMessage(tm.peer, s"Yay! Bot created, here is your token: ${token}")
            case Failure(UserExceptions.NicknameTaken) ⇒
              sendTextMessage(tm.peer, "Nickname already taken")
            case Failure(e) ⇒
              log.error(e, "Failed to create bot")
              sendTextMessage(tm.peer, "There was a problem on our side. Please, try again a bit later.")
          }
        case _ ⇒ sendTextMessage(tm.peer, "Command format is: /bot new <nickname> <name>")
      }
    }
  }
}