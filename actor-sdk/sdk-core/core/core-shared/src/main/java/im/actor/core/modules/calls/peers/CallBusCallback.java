package im.actor.core.modules.calls.peers;

import org.jetbrains.annotations.NotNull;

import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCPeerConnection;

public interface CallBusCallback {

    void onBusStarted(@NotNull String busId);

    void onCallConnected();

    void onCallEnabled();

    void onBusStopped();

    void onPeerConnectionCreated(@NotNull WebRTCPeerConnection peerConnection);

    void onStreamAdded(WebRTCMediaStream stream);

    void onStreamRemoved(WebRTCMediaStream stream);

    void onOwnStreamAdded(WebRTCMediaStream stream);

    void onOwnStreamRemoved(WebRTCMediaStream stream);
}
