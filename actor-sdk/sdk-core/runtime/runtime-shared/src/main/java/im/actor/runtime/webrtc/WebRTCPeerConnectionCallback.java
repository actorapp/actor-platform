package im.actor.runtime.webrtc;

import com.google.j2objc.annotations.ObjectiveCName;

public interface WebRTCPeerConnectionCallback {

    @ObjectiveCName("onCandidateWithLabel:withId:withCandidate:")
    void onCandidate(int label, String id, String candidate);

    @ObjectiveCName("onCandidatesEnd")
    void onCandidatesEnd();

    @ObjectiveCName("onStreamAdded:")
    void onStreamAdded(WebRTCMediaStream stream);

    @ObjectiveCName("onStreamRemoved:")
    void onStreamRemoved(WebRTCMediaStream stream);
}
