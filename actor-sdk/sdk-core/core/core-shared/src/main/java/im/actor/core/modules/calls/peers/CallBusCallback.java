package im.actor.core.modules.calls.peers;

import org.jetbrains.annotations.NotNull;

import im.actor.runtime.webrtc.WebRTCPeerConnection;

public interface CallBusCallback {

    void onBusStarted(@NotNull String busId);

    void onCallConnected();

    void onCallEnabled();

    void onBusStopped();

    void onPeerConnectionCreated(@NotNull WebRTCPeerConnection peerConnection);
}
