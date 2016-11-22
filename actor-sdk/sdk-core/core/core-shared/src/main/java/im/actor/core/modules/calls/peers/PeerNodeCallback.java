package im.actor.core.modules.calls.peers;

import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCMediaTrack;
import im.actor.runtime.webrtc.WebRTCPeerConnection;

/**
 * Peer Node callback
 * HINT: PeerNodeCallback is not subclassed from PeerConnectionCallback because semantics of
 * onStreamAdded/onStreamRemoved is different. This methods are invoked only after
 * onConnectionStarted invoked. Also this interface contains deviceId in each method.
 */
public interface PeerNodeCallback {

    /**
     * Called when new offer arrived
     *
     * @param deviceId  Device Id
     * @param sessionId Session Id
     * @param sdp       sdp of the offer
     */
    void onOffer(long deviceId, long sessionId, String sdp);

    /**
     * Called when new answer arrived
     *
     * @param deviceId  Device Id
     * @param sessionId Session Id
     * @param sdp       sdp of the answer
     */
    void onAnswer(long deviceId, long sessionId, String sdp);

    /**
     * Called when negotiation finished successfully
     *
     * @param deviceId  Device Id
     * @param sessionId Session Id
     */
    void onNegotiationSuccessful(long deviceId, long sessionId);

    /**
     * Called when negotiation needed
     *
     * @param deviceId  Device Id
     * @param sessionId Session Id
     */
    void onNegotiationNeeded(long deviceId, long sessionId);

    /**
     * Called when candidate arrived
     *
     * @param deviceId  Device Id
     * @param sessionId Session Id
     * @param mdpIndex  index of candidate
     * @param id        id of candidate
     * @param sdp       sdp of the answer
     */
    void onCandidate(long deviceId, long sessionId, int mdpIndex, String id, String sdp);

    /**
     * Called when peer state changed
     *
     * @param deviceId Device Id
     */
    void onPeerStateChanged(long deviceId, PeerState state);

    /**
     * Called when track added
     *
     * @param deviceId Device Id
     * @param track    added track
     */
    void onTrackAdded(long deviceId, WebRTCMediaTrack track);

    /**
     * Called when track removed
     *
     * @param deviceId Device Id
     * @param track    removed track
     */
    void onTrackRemoved(long deviceId, WebRTCMediaTrack track);
}
