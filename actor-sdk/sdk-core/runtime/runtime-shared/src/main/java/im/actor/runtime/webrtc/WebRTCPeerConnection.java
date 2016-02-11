package im.actor.runtime.webrtc;

public interface WebRTCPeerConnection {

    void addCallback(WebRTCPeerConnectionCallback callback);

    void removeCallback(WebRTCPeerConnectionCallback callback);

    void addCandidate(int label, String id, String candidate);

    void close();
}
