package im.actor.server.api.rpc.service.webhooks

import akka.actor.ActorSystem
import akka.http.scaladsl.util.FastFuture
import akka.util.Timeout
import im.actor.api.rpc.PeerHelpers._
import im.actor.api.rpc._
import im.actor.api.rpc.integrations.{ IntegrationsService, ResponseIntegrationToken }
import im.actor.api.rpc.peers.{ ApiOutPeer, ApiPeerType }
import im.actor.server.api.rpc.service.webhooks.IntegrationServiceHelpers._
import im.actor.server.db.DbExtension
import im.actor.server.group.GroupErrors.{ NoPermission, NotAMember, NotAdmin }
import im.actor.server.group.GroupExtension
import slick.driver.PostgresDriver.api._

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

class IntegrationsServiceImpl(baseUri: String)(implicit actorSystem: ActorSystem) extends IntegrationsService with PeersImplicits {

  override implicit val ec: ExecutionContext = actorSystem.dispatcher
  private implicit val timeout = Timeout(10.seconds)
  private val db: Database = DbExtension(actorSystem).db
  private val groupExt = GroupExtension(actorSystem)

  private val PeerIsNotGroup = RpcError(403, "PEER_IS_NOT_GROUP", "", false, None)

  override def doHandleGetIntegrationToken(peer: ApiOutPeer, clientData: ClientData): Future[HandlerResult[ResponseIntegrationToken]] =
    authorized(clientData) { implicit client ⇒
      if (peer.`type` != ApiPeerType.Group) {
        FastFuture.successful(Error(PeerIsNotGroup))
      } else {
        withOutPeer(peer) {
          for {
            optToken ← groupExt.getIntegrationToken(peer.id, client.userId)
            (token, url) = optToken map (t ⇒ t → makeUrl(baseUri, t)) getOrElse ("" → "")
          } yield {
            Ok(ResponseIntegrationToken(token, url))
          }
        }
      }
    }

  override def doHandleRevokeIntegrationToken(peer: ApiOutPeer, clientData: ClientData): Future[HandlerResult[ResponseIntegrationToken]] =
    authorized(clientData) { implicit client ⇒
      if (peer.`type` != ApiPeerType.Group) {
        FastFuture.successful(Error(PeerIsNotGroup))
      } else {
        withOutPeer(peer) {
          for {
            token ← groupExt.revokeIntegrationToken(peer.id, client.userId)
          } yield Ok(ResponseIntegrationToken(token, makeUrl(baseUri, token)))
        }
      }
    }

  override def onFailure: PartialFunction[Throwable, RpcError] = {
    case NotAdmin     ⇒ CommonRpcErrors.forbidden("Only admin can perform this action.")
    case NotAMember   ⇒ CommonRpcErrors.forbidden("You are not a group member.")
    case NoPermission ⇒ CommonRpcErrors.forbidden("You have no permission to execute this action")
  }

}
