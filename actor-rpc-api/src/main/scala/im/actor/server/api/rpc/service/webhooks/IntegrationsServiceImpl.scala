package im.actor.server.api.rpc.service.webhooks

import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.ActorSystem
import slick.driver.PostgresDriver.api._

import im.actor.api.PeersImplicits
import im.actor.api.rpc.PeerHelpers._
import im.actor.api.rpc.integrtions.{ IntegrtionsService, ResponseIntegrationToken }
import im.actor.api.rpc.peers.OutPeer
import im.actor.api.rpc.{ ClientData, _ }
import IntegrationServiceHelpers._
import im.actor.server.api.http.HttpApiConfig
import im.actor.server.persist
import im.actor.server.util.ACLUtils

class IntegrationsServiceImpl(config: HttpApiConfig)(implicit db: Database, actorSystem: ActorSystem) extends IntegrtionsService with PeersImplicits {

  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  override def jhandleGetIntegrationToken(groupPeer: OutPeer, clientData: ClientData): Future[HandlerResult[ResponseIntegrationToken]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      withOutPeerAsGroupPeer(groupPeer) { groupOutPeer ⇒
        withOwnGroupMember(groupOutPeer, client.userId) { fullGroup ⇒
          for {
            optGroupBot ← persist.GroupBot.findByGroup(fullGroup.id)
          } yield optGroupBot.map(b ⇒ Ok(ResponseIntegrationToken(b.token, makeUrl(config, b.token)))).getOrElse(Error(TokenNotFound))
        }
      }
    }
    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleRevokeIntegrationToken(groupPeer: OutPeer, clientData: ClientData): Future[HandlerResult[ResponseIntegrationToken]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      withOutPeerAsGroupPeer(groupPeer) { groupOutPeer ⇒
        withGroupAdmin(groupOutPeer) { fullGroup ⇒
          val newToken = ACLUtils.accessToken(ThreadLocalRandom.current())
          for {
            _ ← persist.GroupBot.updateToken(fullGroup.id, newToken)
          } yield Ok(ResponseIntegrationToken(newToken, makeUrl(config, newToken)))
        }
      }
    }
    db.run(toDBIOAction(authorizedAction))
  }

}
