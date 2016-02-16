package im.actor.server.api.rpc.service.webrtc

import akka.actor.ActorSystem
import im.actor.api.rpc._
import im.actor.api.rpc.peers.{ ApiPeerType, ApiPeer, ApiOutPeer }
import im.actor.api.rpc.webrtc.{ ResponseGetCallInfo, ResponseDoCall, WebrtcService }
import im.actor.concurrent.FutureExt
import im.actor.server.acl.ACLUtils
import im.actor.server.session._
import im.actor.server.webrtc.{ WebrtcCallErrors, WebrtcExtension }

import scala.concurrent.{ ExecutionContext, Future }

object WebrtcErrors {
  val CallNotStarted = RpcError(400, "CALL_NOT_STARTED", "Call not started.", canTryAgain = false, None)
  val CallAlreadyStareted = RpcError(400, "CALL_ALREADY_STARTED", "Call already started", canTryAgain = false, None)
  val NotAParticipant = RpcError(403, "NOT_A_PARTICIPANT", "Not a participant", canTryAgain = false, None)
}

final class WebrtcServiceImpl(implicit system: ActorSystem, sessionRegion: SessionRegion) extends WebrtcService {
  import PeerHelpers._

  override implicit protected val ec: ExecutionContext = system.dispatcher

  val webrtcExt = WebrtcExtension(system)

  override def doHandleGetCallInfo(callId: Long, clientData: ClientData): Future[HandlerResult[ResponseGetCallInfo]] =
    authorized(clientData) { client ⇒
      for {
        (eventBusId, callerUserId, participants) ← webrtcExt.getInfo(callId)
        users ← FutureExt.ftraverse(participants)(ACLUtils.getUserOutPeer(_, client.authId))
      } yield Ok(ResponseGetCallInfo(ApiPeer(ApiPeerType.Private, callerUserId), Vector.empty, users.toVector, eventBusId))
    }

  override def doHandleDoCall(peer: ApiOutPeer, eventBusId: String, clientData: ClientData): Future[HandlerResult[ResponseDoCall]] =
    authorized(clientData) { implicit client ⇒
      withOutPeerF(peer) {
        for {
          callId ← webrtcExt.doCall(client.userId, peer.asModel, eventBusId)
        } yield Ok(ResponseDoCall(callId))
      }
    }

  override def onFailure: PartialFunction[Throwable, RpcError] = {
    case WebrtcCallErrors.CallAlreadyStarted ⇒ WebrtcErrors.CallAlreadyStareted
  }

}