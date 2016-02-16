package im.actor.server.api.rpc.service.webhooks

import akka.actor.ActorSystem
import akka.util.Timeout
import im.actor.api.rpc.PeerHelpers._
import im.actor.api.rpc._
import im.actor.api.rpc.integrations.{ ResponseIntegrationToken, IntegrationsService }
import im.actor.api.rpc.peers.ApiOutPeer
import im.actor.server.api.rpc.service.webhooks.IntegrationServiceHelpers._
import im.actor.server.db.DbExtension
import im.actor.server.group.GroupErrors.{ NotAMember, NotAdmin }
import im.actor.server.group.GroupExtension
import slick.driver.PostgresDriver.api._

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

class IntegrationsServiceImpl(baseUri: String)(implicit actorSystem: ActorSystem) extends IntegrationsService with PeersImplicits {

  override implicit val ec: ExecutionContext = actorSystem.dispatcher
  private implicit val timeout = Timeout(10.seconds)
  private val db: Database = DbExtension(actorSystem).db
  private val groupExt = GroupExtension(actorSystem)

  override def doHandleGetIntegrationToken(groupPeer: ApiOutPeer, clientData: ClientData): Future[HandlerResult[ResponseIntegrationToken]] =
    authorized(clientData) { implicit client ⇒
      val action = withOutPeerAsGroupPeer(groupPeer) { groupOutPeer ⇒
        for {
          optToken ← DBIO.from(groupExt.getIntegrationToken(groupOutPeer.groupId, client.userId))
        } yield {
          val (token, url) = optToken map (t ⇒ t → makeUrl(baseUri, t)) getOrElse ("" → "")
          Ok(ResponseIntegrationToken(token, url))
        }
      }
      db.run(action)
    }

  override def doHandleRevokeIntegrationToken(groupPeer: ApiOutPeer, clientData: ClientData): Future[HandlerResult[ResponseIntegrationToken]] =
    authorized(clientData) { implicit client ⇒
      val action = withOutPeerAsGroupPeer(groupPeer) { groupOutPeer ⇒
        for {
          token ← DBIO.from(groupExt.revokeIntegrationToken(groupOutPeer.groupId, client.userId))
        } yield Ok(ResponseIntegrationToken(token, makeUrl(baseUri, token)))
      }
      db.run(action)
    }

  override def onFailure: PartialFunction[Throwable, RpcError] = {
    case NotAdmin   ⇒ CommonRpcErrors.forbidden("Only admin can perform this action.")
    case NotAMember ⇒ CommonRpcErrors.forbidden("You are not a group member.")
  }

}
