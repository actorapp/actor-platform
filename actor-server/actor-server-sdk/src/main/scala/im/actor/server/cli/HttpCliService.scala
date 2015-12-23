package im.actor.server.cli

import akka.actor.{ Props, Actor }
import akka.cluster.client.ClusterClientReceptionist
import akka.pattern.pipe
import im.actor.server.acl.ACLUtils
import im.actor.server.db.DbExtension
import im.actor.server.persist.HttpApiTokenRepo

object HttpCliService {
  def props = Props(new HttpCliService)
}

final class HttpCliService extends Actor {
  import context.dispatcher

  ClusterClientReceptionist(context.system).registerService(self)

  private val db = DbExtension(context.system).db

  def receive = {
    case CreateApiToken(isAdmin) ⇒
      val token = ACLUtils.accessToken()
      (for {
        _ ← db.run(HttpApiTokenRepo.create(token, isAdmin = isAdmin))
      } yield CreateApiTokenResponse(token)) pipeTo sender()
  }
}