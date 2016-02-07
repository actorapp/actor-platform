package im.actor.core.webrtc;

import im.actor.core.Messenger;

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
     * @param messenger  Messenger
     * @param controller controller
     */
    void init(Messenger messenger, WebRTCController controller);

    /**
     * Incoming Call event. To answer call invoke controller.answerCall();
     *
     * @param callId Unique Call Id
     */
    void onIncomingCall(long callId);

    /**
     * Outgoing Call event. This doesn't mean that call is started.
     *
     * @param callId Unique Call Id
     */
    void onOutgoingCall(long callId);

    /**
     * Called when WebRTC need to send offer
     *
     * @param callId Unique Call Id
     */
    void onOfferNeeded(long callId);

    /**
     * Called when Answer received
     *
     * @param callId   Unique Call Id
     * @param offerSDP answer SDP
     */
    void onAnswerReceived(long callId, String offerSDP);

    /**
     * Called when call offer arrived
     *
     * @param callId   Unique Call Id
     * @param offerSDP offer SDP
     */
    void onOfferReceived(long callId, String offerSDP);

    /**
     * Called when new candidate arrived from other peer
     *
     * @param callId Unique Call Id
     * @param id     id of candidate
     * @param label  label of candidate
     * @param sdp    sdp of candidate
     */
    void onCandidate(long callId, String id, int label, String sdp);

    /**
     * Call End event
     *
     * @param callId Unique Call Id
     */
    void onCallEnd(long callId);
}