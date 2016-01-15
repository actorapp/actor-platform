package im.actor.server.api.rpc.service.webrtc

import akka.actor.ActorSystem
import akka.http.scaladsl.util.FastFuture
import im.actor.api.rpc._
import im.actor.api.rpc.misc.ResponseVoid
import im.actor.api.rpc.peers.ApiOutPeer
import im.actor.api.rpc.webrtc.{ ResponseDoCall, WebrtcService }
import im.actor.server.session._
import im.actor.server.webrtc.{ WebrtcCallErrors, WebrtcExtension, Webrtc }

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

  override def jhandleDoCall(peer: ApiOutPeer, timeout: Int, clientData: ClientData): Future[HandlerResult[ResponseDoCall]] =
    authorized(clientData) { implicit client ⇒
      withOutPeerF(peer) {
        (for {
          callId ← webrtcExt.doCall(client.userId, peer.id)
        } yield Ok(ResponseDoCall(callId))) recover {
          case WebrtcCallErrors.CallAlreadyStarted ⇒ Error(WebrtcErrors.CallAlreadyStareted)
        }
      }
    }

  override def jhandleEndCall(callId: Long, clientData: ClientData): Future[HandlerResult[ResponseVoid]] =
    authorized(clientData) { client ⇒
      (for {
        _ ← webrtcExt.endCall(client.userId, callId)
      } yield Ok(ResponseVoid)) recover {
        case WebrtcCallErrors.CallNotStarted ⇒ Error(WebrtcErrors.CallNotStarted)
      }
    }

  override def jhandleUnsubscribeToCalls(clientData: ClientData): Future[HandlerResult[ResponseVoid]] =
    authorized(clientData) { client ⇒
      sessionRegion.ref !
        SessionEnvelope(clientData.authId, clientData.sessionId)
        .withUnsubscribeFromWeak(UnsubscribeFromWeak(Some(Webrtc.WeakGroup)))
      FastFuture.successful(Ok(ResponseVoid))
    }

  override def jhandleCallInProgress(callId: Long, timeout: Int, clientData: ClientData): Future[HandlerResult[ResponseVoid]] =
    authorized(clientData) { client ⇒
      (for {
        _ ← webrtcExt.sendCallInProgress(client.userId, callId, timeout)
      } yield Ok(ResponseVoid)) recover {
        case WebrtcCallErrors.CallNotStarted ⇒ Error(WebrtcErrors.CallNotStarted)
      }
    }

  override def jhandleSubscribeToCalls(clientData: ClientData): Future[HandlerResult[ResponseVoid]] =
    authorized(clientData) { client ⇒
      sessionRegion.ref !
        SessionEnvelope(clientData.authId, clientData.sessionId)
        .withSubscribeToWeak(SubscribeToWeak(Some(Webrtc.WeakGroup)))
      FastFuture.successful(Ok(ResponseVoid))
    }

  override def jhandleSendCallSignal(callId: Long, content: Array[Byte], clientData: ClientData): Future[HandlerResult[ResponseVoid]] =
    authorized(clientData) { client ⇒
      (for {
        _ ← webrtcExt.sendCallSignal(client.userId, callId, content)
      } yield Ok(ResponseVoid)) recover {
        case WebrtcCallErrors.CallNotStarted ⇒ Error(WebrtcErrors.CallNotStarted)
      }
    }
}