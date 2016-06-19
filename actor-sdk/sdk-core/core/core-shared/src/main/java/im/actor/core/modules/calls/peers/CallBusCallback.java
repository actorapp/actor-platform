package im.actor.core.modules.calls.peers;

import org.jetbrains.annotations.NotNull;

import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCPeerConnection;

public interface CallBusCallback {

    void onBusStarted(@NotNull String busId);

    void onCallConnected();

    void onCallEnabled();

    void onBusStopped();


    //
    // Peer Connection Callbacks
    //

    void onPeerConnectionStateChanged(long deviceId, boolean isAudioEnabled, boolean isVideoEnabled);

    void onStreamAdded(long deviceId, WebRTCMediaStream stream);

    void onStreamRemoved(long deviceId, WebRTCMediaStream stream);

    void onPeerConnectionDisposed(long deviceId);


    //
    // Own Streams
    //

    void onOwnStreamAdded(WebRTCMediaStream stream);

    void onOwnStreamRemoved(WebRTCMediaStream stream);
}
