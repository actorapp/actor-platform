package im.actor.core.webrtc;

import im.actor.core.entity.Peer;
import im.actor.core.entity.signals.AbsSignal;
import im.actor.core.viewmodel.UserVM;

/**
 * WebRTC provider. Used for providing Calls support.
 * All methods except init are called in background call management actor.
 * IMPORTANT: Right after "onCallEnd" called you need to stop sending any signaling messages.
 * Between onIncomingCall/onOutgoingCall and onCallEnd all methods are called with the same call id.
 */
public interface WebRTCProvider {

    /**
     * Init WebRTC provider
     *
     * @param controller controller
     */
    void init(WebRTCController controller);

    /**
     * Incoming Call event. To answer call invoke controller.answerCall();
     *
     * @param callId Unique Call Id
     * @param peer   Call peer
     * @param users  participators of a call
     */
    void onIncomingCall(long callId, Peer peer, UserVM[] users);

    /**
     * Outgoing Call event. This doesn't mean that call is started.
     *
     * @param peer  Call peer
     * @param users participators of a call
     */
    void onOutgoingCall(long callId, Peer peer, UserVM[] users);

    /**
     * Call Start event. Called when they other peer answers a call.
     */
    void onCallStart(long callId);

    /**
     * Call signaling event. When other peer sends signaling.
     *
     * @param signal signal
     */
    void onCallSignaling(long callId, AbsSignal signal);

    /**
     * Call End event
     */
    void onCallEnd(long callId);
}