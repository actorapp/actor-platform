package im.actor.core.modules.calls.peers;

import im.actor.runtime.webrtc.WebRTCMediaTrack;

public interface PeerCallCallback {


    void onOffer(long deviceId, long sessionId, String sdp);

    void onAnswer(long deviceId, long sessionId, String sdp);

    void onCandidate(long deviceId, long sessionId, int mdpIndex, String id, String sdp);

    void onNegotiationSuccessful(long deviceId, long sessionId);

    void onNegotiationNeeded(long deviceId, long sessionId);

    void onMediaStreamsChanged(long deviceId, boolean isAudioEnabled, boolean isVideoEnabled);

    void onTrackAdded(long deviceId, WebRTCMediaTrack track);

    void onTrackRemoved(long deviceId, WebRTCMediaTrack track);

    void onOwnTrackAdded(WebRTCMediaTrack track);

    void onOwnTrackRemoved(WebRTCMediaTrack track);


    void onPeerStateChanged(long deviceId, PeerState state);
}