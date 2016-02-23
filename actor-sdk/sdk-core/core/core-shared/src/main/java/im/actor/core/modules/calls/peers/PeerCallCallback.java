package im.actor.core.modules.calls.peers;

import im.actor.runtime.webrtc.WebRTCMediaStream;

public interface PeerCallCallback {

    void onOffer(long deviceId, String sdp);

    void onAnswer(long deviceId, String sdp);

    void onCandidate(long deviceId, int mdpIndex, String id, String sdp);

    void onPeerStateChanged(long deviceId, PeerState state);

    void onStreamAdded(long deviceId, WebRTCMediaStream stream);

    void onStreamRemoved(long deviceId, WebRTCMediaStream stream);
}