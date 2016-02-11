package im.actor.runtime.webrtc;

public interface WebRTCPeerConnectionCallback {
    void onCandidate(int label, String id, String candidate);
}
