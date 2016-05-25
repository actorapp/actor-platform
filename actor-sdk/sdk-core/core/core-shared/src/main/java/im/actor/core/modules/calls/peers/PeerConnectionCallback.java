package im.actor.core.modules.calls.peers;

import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCPeerConnection;

/**
 * Peer Connection Callback
 */
public interface PeerConnectionCallback {

    /**
     * Called when offer need to be sent to other peer
     *
     * @param sdp sdp of the offer
     */
    void onOffer(long sessionId, String sdp);

    /**
     * Called when answer need to be sent to other peer
     *
     * @param sdp sdp of the answer
     */
    void onAnswer(long sessionId, String sdp);

    /**
     * Called when new ICE candidate was found
     *
     * @param mdpIndex index in source SDP line
     * @param id       id of candidate
     * @param sdp      sdp of candidate
     */
    void onCandidate(int mdpIndex, String id, String sdp);

    /**
     * Called when negotiation finished successfully
     */
    void onNegotiationSuccessful(long sessionId);

    /**
     * Called when peer stream was added
     *
     * @param stream added stream
     */
    void onStreamAdded(WebRTCMediaStream stream);

    /**
     * Called when peer was removed
     *
     * @param stream removed stream
     */
    void onStreamRemoved(WebRTCMediaStream stream);

    void onPeerConnectionCreated(WebRTCPeerConnection peerConnection);
}
