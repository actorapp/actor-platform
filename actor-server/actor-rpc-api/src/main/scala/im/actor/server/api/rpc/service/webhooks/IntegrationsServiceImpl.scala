package im.actor.server.api.rpc.service.webhooks

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.ActorSystem
import akka.util.Timeout
import slick.driver.PostgresDriver.api._

import im.actor.api.PeersImplicits
import im.actor.api.rpc.PeerHelpers._
import im.actor.api.rpc.integrtions.{ IntegrtionsService, ResponseIntegrationToken }
import im.actor.api.rpc.peers.OutPeer
import im.actor.api.rpc.{ ClientData, _ }
import im.actor.server.api.http.HttpApiConfig
import im.actor.server.api.rpc.service.webhooks.IntegrationServiceHelpers._
import im.actor.server.group.GroupErrors.{ NotAMember, NotAdmin }
import im.actor.server.group.{ GroupOffice, GroupProcessorRegion }

class IntegrationsServiceImpl(config: HttpApiConfig)(
  implicit
  db:                Database,
  groupOfficeRegion: GroupProcessorRegion,
  actorSystem:       ActorSystem
) extends IntegrtionsService with PeersImplicits {

  override implicit val ec: ExecutionContext = actorSystem.dispatcher
  private implicit val timeout = Timeout(10.seconds)

  override def jhandleGetIntegrationToken(groupPeer: OutPeer, clientData: ClientData): Future[HandlerResult[ResponseIntegrationToken]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      withOutPeerAsGroupPeer(groupPeer) { groupOutPeer ⇒
        for {
          optToken ← DBIO.from(GroupOffice.getIntegrationToken(groupOutPeer.groupId, client.userId))
        } yield optToken.map(token ⇒ Ok(ResponseIntegrationToken(token, makeUrl(config, token)))).getOrElse(Error(TokenNotFound))
      }
    }
    db.run(toDBIOAction(authorizedAction)) recover {
      case NotAMember ⇒ Error(CommonErrors.forbidden("You are not a group member."))
    }
  }

  override def jhandleRevokeIntegrationToken(groupPeer: OutPeer, clientData: ClientData): Future[HandlerResult[ResponseIntegrationToken]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      withOutPeerAsGroupPeer(groupPeer) { groupOutPeer ⇒
        for {
          token ← DBIO.from(GroupOffice.revokeIntegrationToken(groupOutPeer.groupId, client.userId))
        } yield Ok(ResponseIntegrationToken(token, makeUrl(config, token)))
      }
    }
    db.run(toDBIOAction(authorizedAction)) recover {
      case NotAdmin ⇒ Error(CommonErrors.forbidden("Only admin can perform this action."))
    }
  }

}
