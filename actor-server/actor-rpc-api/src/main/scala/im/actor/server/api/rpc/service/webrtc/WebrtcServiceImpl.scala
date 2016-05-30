package im.actor.server.api.rpc.service.webrtc

import akka.actor.ActorSystem
import akka.http.scaladsl.util.FastFuture
import im.actor.api.rpc._
import im.actor.api.rpc.misc.ResponseVoid
import im.actor.api.rpc.peers._
import im.actor.api.rpc.webrtc._
import im.actor.concurrent.FutureExt
import im.actor.server.acl.ACLUtils
import im.actor.server.session._
import im.actor.server.webrtc.{ WebrtcCallErrors, WebrtcExtension }

import scala.concurrent.{ ExecutionContext, Future }

object WebrtcErrors {
  val CallNotStarted = RpcError(400, "CALL_NOT_STARTED", "Call not started.", canTryAgain = false, None)
  val CallAlreadyStareted = RpcError(400, "CALL_ALREADY_STARTED", "Call already started.", canTryAgain = false, None)
  val NotAParticipant = RpcError(403, "NOT_A_PARTICIPANT", "Not a participant.", canTryAgain = false, None)
  val NotJoinedToEventBus = RpcError(400, "NOT_JOINED_TO_EVENT_BUS", "Not joined to event bus.", canTryAgain = false, None)
  val CallForbidden = RpcError(403, "CALL_FORBIDDEN", "You are forbidden to call this user", canTryAgain = false, None)
  val GroupTooBig = RpcError(400, "GROUP_TOO_BIG", "Group is too big for group call", canTryAgain = false, None)
}

final class WebrtcServiceImpl(implicit system: ActorSystem, sessionRegion: SessionRegion) extends WebrtcService {
  import PeerHelpers._

  override implicit protected val ec: ExecutionContext = system.dispatcher

  val webrtcExt = WebrtcExtension(system)

  override def doHandleGetCallInfo(callId: Long, clientData: ClientData): Future[HandlerResult[ResponseGetCallInfo]] =
    authorized(clientData) { client ⇒
      for {
        (eventBusId, peer, participants) ← webrtcExt.getInfo(callId)
        users ← FutureExt.ftraverse(participants)(ACLUtils.getUserOutPeer(_, client.authId))
      } yield Ok(ResponseGetCallInfo(peer.asStruct, Vector.empty, users.toVector, eventBusId))
    }

  override def doHandleDoCall(peer: ApiOutPeer, timeout: Option[Long], clientData: ClientData): Future[HandlerResult[ResponseDoCall]] =
    authorized(clientData) { implicit client ⇒
      withOutPeer(peer) {
        for {
          (callId, eventBusId, callerDeviceId) ← webrtcExt.doCall(client.userId, client.authId, peer.asModel, timeout)
        } yield Ok(ResponseDoCall(callId, eventBusId, callerDeviceId))
      }
    }

  /**
   * Rejecting Call
   *
   * @param callId Call Id
   */
  override protected def doHandleRejectCall(
    callId:     Long,
    clientData: ClientData
  ): Future[HandlerResult[ResponseVoid]] =
    authorized(clientData) { implicit client ⇒
      for {
        callId ← webrtcExt.rejectCall(client.userId, client.authId, callId)
      } yield Ok(ResponseVoid)
    }

  /**
   * Joining Call
   *
   * @param callId Call Id
   */
  override protected def doHandleJoinCall(
    callId:     Long,
    clientData: ClientData
  ): Future[HandlerResult[ResponseVoid]] =
    authorized(clientData) { implicit client ⇒
      for {
        callId ← webrtcExt.joinCall(client.userId, client.authId, callId)
      } yield Ok(ResponseVoid)
    }

  /**
   * Method for upgrading a call from private call to group call
   *
   * @param callId Call Id
   * @param peer   Destination peer for upgrading
   */
  override protected def doHandleUpgradeCall(
    callId:     Long,
    peer:       ApiGroupOutPeer,
    clientData: ClientData
  ): Future[HandlerResult[ResponseVoid]] = FastFuture.failed(new RuntimeException("Not implemented"))

  /**
   * Call again to user
   *
   * @param callId Call Id
   * @param user   User to call again
   */
  override protected def doHandleDoCallAgain(
    callId:     Long,
    user:       ApiUserOutPeer,
    clientData: ClientData
  ): Future[HandlerResult[ResponseVoid]] = FastFuture.failed(new RuntimeException("Not implemented"))

  /**
   * Optimizing SDP
   *
   * @param type          Type of SDP (offer or answer)
   * @param sdp           SDP value
   * @param ownSettings   Own Settings
   * @param theirSettings Their Settings
   */
  override protected def doHandleOptimizeSDP(
    `type`:        String,
    sdp:           String,
    ownSettings:   ApiPeerSettings,
    theirSettings: ApiPeerSettings,
    clientData:    ClientData
  ): Future[HandlerResult[ResponseOptimizeSDP]] = FastFuture.failed(new RuntimeException("Not implemented"))

  override def onFailure: PartialFunction[Throwable, RpcError] = {
    case WebrtcCallErrors.CallAlreadyStarted  ⇒ WebrtcErrors.CallAlreadyStareted
    case WebrtcCallErrors.CallNotStarted      ⇒ WebrtcErrors.CallNotStarted
    case WebrtcCallErrors.NotAParticipant     ⇒ WebrtcErrors.NotAParticipant
    case WebrtcCallErrors.NotJoinedToEventBus ⇒ WebrtcErrors.NotJoinedToEventBus
    case WebrtcCallErrors.CallForbidden       ⇒ WebrtcErrors.CallForbidden
    case WebrtcCallErrors.GroupTooBig         ⇒ WebrtcErrors.GroupTooBig
  }
}