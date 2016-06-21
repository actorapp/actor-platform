package im.actor.runtime;

import im.actor.runtime.promise.Promise;
import im.actor.runtime.webrtc.WebRTCIceServer;
import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCPeerConnection;
import im.actor.runtime.webrtc.WebRTCSettings;
import org.jetbrains.annotations.NotNull;

public class WebRTCRuntimeProvider implements WebRTCRuntime {

    @NotNull
    @Override
    public Promise<WebRTCPeerConnection> createPeerConnection(WebRTCIceServer[] webRTCIceServers, WebRTCSettings settings) {
        return null;
    }

    @NotNull
    @Override
    public Promise<WebRTCMediaStream> getUserMedia(boolean isAudioEnabled, boolean isVideoEnabled) {
        return null;
    }

    @Override
    public boolean supportsPreConnections() {
        return false;
    }
}
