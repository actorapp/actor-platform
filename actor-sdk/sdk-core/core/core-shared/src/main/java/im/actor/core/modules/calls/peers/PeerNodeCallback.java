package im.actor.core.modules.calls.peers;

import im.actor.runtime.webrtc.WebRTCMediaStream;
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
     * @param sessionId Session If
     */
    void onNegotiationSuccessful(long deviceId, long sessionId);

    /**
     * Called when candidate arrived
     *
     * @param deviceId Device Id
     * @param mdpIndex index of candidate
     * @param id       id of candidate
     * @param sdp      sdp of the answer
     */
    void onCandidate(long deviceId, int mdpIndex, String id, String sdp);

    /**
     * Called when peer state changed
     *
     * @param deviceId Device Id
     */
    void onPeerStateChanged(long deviceId, PeerState state);

    /**
     * Called when stream added
     *
     * @param deviceId Device Id
     * @param stream   added stream
     */
    void onStreamAdded(long deviceId, WebRTCMediaStream stream);

    /**
     * Called when stream removed
     *
     * @param deviceId Device Id
     * @param stream   removed strea
     */
    void onStreamRemoved(long deviceId, WebRTCMediaStream stream);

    void onPeerConnectionCreated(WebRTCPeerConnection peerConnection);
}
