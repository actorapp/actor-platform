package im.actor.core.modules.calls.peers;

import im.actor.runtime.webrtc.WebRTCMediaStream;

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
     * @param deviceId Device Id
     * @param sdp      sdp of the offer
     */
    void onOffer(long deviceId, String sdp);

    /**
     * Called when new answer arrived
     *
     * @param deviceId Device Id
     * @param sdp      sdp of the answer
     */
    void onAnswer(long deviceId, String sdp);

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
     * Called when connection started
     *
     * @param deviceId Device Id
     */
    void onConnectionStarted(long deviceId);

    /**
     * Called when connection established
     *
     * @param deviceId Device Id
     */
    void onConnectionEstablished(long deviceId);

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
     * @param stream   removed stream
     */
    void onStreamRemoved(long deviceId, WebRTCMediaStream stream);
}
