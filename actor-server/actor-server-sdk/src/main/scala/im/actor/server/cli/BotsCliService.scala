package im.actor.server.cli

import akka.actor.{ Props, Actor, ActorLogging }
import akka.cluster.client.ClusterClientReceptionist
import akka.pattern.pipe
import im.actor.server.bot.BotExtension

object BotsCliService {
  def props = Props(new BotsCliService)
}

private final class BotsCliService extends Actor with ActorLogging {
  import context.dispatcher

  ClusterClientReceptionist(context.system).registerService(self)

  private val botExt = BotExtension(context.system)

  def receive = {
    case CreateBot(username, name, isAdmin) ⇒
      (for {
        (token, _) ← botExt.create(username, name, isAdmin)
      } yield CreateBotResponse(token)) pipeTo sender()
  }
}
