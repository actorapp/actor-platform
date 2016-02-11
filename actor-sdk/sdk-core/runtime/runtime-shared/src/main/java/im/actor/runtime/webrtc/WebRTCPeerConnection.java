package im.actor.runtime.webrtc;

public interface WebRTCPeerConnection {

    void mute();

    void unmute();

    void addCandidate(int label, String id, String candidate);

    void close();
}
