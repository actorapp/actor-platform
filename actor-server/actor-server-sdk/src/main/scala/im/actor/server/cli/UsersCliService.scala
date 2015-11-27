package im.actor.server.cli

import akka.actor.{ Props, Actor, ActorLogging }
import akka.cluster.client.ClusterClientReceptionist
import akka.pattern.pipe
import im.actor.server.user.UserExtension

object UsersCliService {
  def props = Props(new UsersCliService)
}

private final class UsersCliService extends Actor with ActorLogging {
  import context.dispatcher

  ClusterClientReceptionist(context.system).registerService(self)

  private val userExt = UserExtension(context.system)

  def receive = {
    case UpdateIsAdmin(userId, isAdmin) ⇒
      (for {
        _ ← userExt.updateIsAdmin(userId, isAdmin)
      } yield UpdateIsAdminResponse()) pipeTo sender()
  }
}
