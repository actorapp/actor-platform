package im.actor.core.modules.calls.peers;

import im.actor.runtime.webrtc.WebRTCMediaStream;

public interface PeerNodeCallback {

    void onOffer(long deviceId, String sdp);

    void onAnswer(long deviceId, String sdp);

    void onCandidate(long deviceId, int mdpIndex, String id, String sdp);

    void onConnectionStarted(long deviceId);

    void onConnectionEstablished(long deviceId);

    void onStreamAdded(long deviceId, WebRTCMediaStream stream);

    void onStreamRemoved(long deviceId, WebRTCMediaStream stream);
}
