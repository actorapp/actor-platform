package im.actor.core.modules.calls.peers;

import im.actor.runtime.webrtc.WebRTCMediaStream;

public interface PeerConnectionCallback {

    void onOffer(String sdp);

    void onAnswer(String sdp);

    void onCandidate(int mdpIndex, String id, String sdp);

    void onStreamAdded(WebRTCMediaStream stream);

    void onStreamRemoved(WebRTCMediaStream stream);
}
