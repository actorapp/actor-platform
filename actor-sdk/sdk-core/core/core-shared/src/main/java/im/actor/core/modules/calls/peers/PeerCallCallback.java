package im.actor.core.modules.calls.peers;

import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCPeerConnection;

public interface PeerCallCallback {

    void onOffer(long deviceId, long sessionId, String sdp);

    void onAnswer(long deviceId, long sessionId, String sdp);

    void onCandidate(long deviceId, int mdpIndex, String id, String sdp);

    void onNegotiationSuccessful(long deviceId, long sessionId);

    void onPeerStateChanged(long deviceId, PeerState state);

    void onStreamAdded(long deviceId, WebRTCMediaStream stream);

    void onStreamRemoved(long deviceId, WebRTCMediaStream stream);

    void onPeerConnectionCreated(WebRTCPeerConnection peerConnection);
}