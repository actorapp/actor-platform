package im.actor.core.webrtc;

import com.google.j2objc.annotations.ObjectiveCName;

import org.jetbrains.annotations.NotNull;

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
    @ObjectiveCName("initWithMessenger:withController:")
    void init(@NotNull Messenger messenger, @NotNull WebRTCController controller);

    /**
     * Incoming Call event. To answer call invoke controller.answerCall();
     *
     * @param callId Unique Call Id
     */
    @ObjectiveCName("onIncomingCallWithCallId:")
    void onIncomingCall(long callId);

    /**
     * Outgoing Call event. This doesn't mean that call is started.
     *
     * @param callId Unique Call Id
     */
    @ObjectiveCName("onOutgoingCallWithCallId:")
    void onOutgoingCall(long callId);

    /**
     * Called when WebRTC need to send offer
     *
     * @param callId Unique Call Id
     */
    @ObjectiveCName("onOfferNeededWithCallId:")
    void onOfferNeeded(long callId);

    /**
     * Called when Answer received
     *
     * @param callId   Unique Call Id
     * @param offerSDP answer SDP
     */
    @ObjectiveCName("onAnswerReceivedWithCallId:withSDP:")
    void onAnswerReceived(long callId, @NotNull String offerSDP);

    /**
     * Called when call offer arrived
     *
     * @param callId   Unique Call Id
     * @param offerSDP offer SDP
     */
    @ObjectiveCName("onOfferReceivedWithCallId:withSDP:")
    void onOfferReceived(long callId, @NotNull String offerSDP);

    /**
     * Called when new candidate arrived from other peer
     *
     * @param callId Unique Call Id
     * @param id     id of candidate
     * @param label  label of candidate
     * @param sdp    sdp of candidate
     */
    @ObjectiveCName("onCandidateWithCallId:withId:withLabel:withSDP:")
    void onCandidate(long callId, @NotNull String id, int label, @NotNull String sdp);

    /**
     * Call End event
     *
     * @param callId Unique Call Id
     */
    @ObjectiveCName("onCallEndWithCallId:")
    void onCallEnd(long callId);
}