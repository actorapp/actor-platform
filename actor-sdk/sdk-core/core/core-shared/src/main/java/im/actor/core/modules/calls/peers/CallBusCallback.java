package im.actor.core.modules.calls.peers;

import org.jetbrains.annotations.NotNull;

import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCMediaTrack;
import im.actor.runtime.webrtc.WebRTCPeerConnection;

public interface CallBusCallback {

    //
    // Event Bus
    //

    void onBusStarted(@NotNull String busId);

    void onBusStopped();

    //
    // Call Events
    //

    void onCallConnected();

    void onCallEnabled();

    //
    // Peer Connection Callbacks
    //

    void onTrackAdded(long deviceId, WebRTCMediaTrack track);

    void onTrackRemoved(long deviceId, WebRTCMediaTrack track);

    //
    // Own Streams
    //

    void onOwnTrackAdded(WebRTCMediaTrack track);

    void onOwnTrackRemoved(WebRTCMediaTrack track);
}
