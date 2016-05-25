package im.actor.runtime.webrtc;

import com.google.j2objc.annotations.ObjectiveCName;

/**
 * WebRTC Peer Connection Callback for handling peer connection events
 */
public interface WebRTCPeerConnectionCallback {

    /**
     * On Got ICE candidate
     *
     * @param label     index of candidate
     * @param id        id of candidate
     * @param candidate candidate
     */
    @ObjectiveCName("onCandidateWithLabel:withId:withCandidate:")
    void onCandidate(int label, String id, String candidate);

    /**
     * On Remote Stream Added
     *
     * @param stream stream
     */
    @ObjectiveCName("onStreamAdded:")
    void onStreamAdded(WebRTCMediaStream stream);

    /**
     * On Remote Stream Removed
     *
     * @param stream removed stream. References can be different from that passed in onStreamAdded
     */
    @ObjectiveCName("onStreamRemoved:")
    void onStreamRemoved(WebRTCMediaStream stream);

    /**
     * When renegotiation is needed
     */
    @ObjectiveCName("onRenegotiationNeeded")
    void onRenegotiationNeeded();

    /**
     * On Own Stream Added
     *
     * @param stream stream
     */
    @ObjectiveCName("onOwnStreamAdded:")
    void onOwnStreamAdded(WebRTCMediaStream stream);

    /**
     * On Own Stream Removed
     *
     * @param stream removed stream. References can be different from that passed in onStreamAdded
     */
    @ObjectiveCName("onOwnStreamRemoved:")
    void onOwnStreamRemoved(WebRTCMediaStream stream);


    /**
     * Peer connection disposed
     */
    @ObjectiveCName("onDisposed")
    void onDisposed();


}