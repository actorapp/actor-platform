package im.actor.runtime;

import im.actor.runtime.promise.Promise;
import im.actor.runtime.webrtc.WebRTCIceServer;
import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCPeerConnection;
import im.actor.runtime.webrtc.WebRTCSettings;
import org.jetbrains.annotations.NotNull;

/**
 * Created by elenoon on 3/1/16.
 */
public class WebRTCRuntimeProvider implements WebRTCRuntime {
    @NotNull
    @Override
    public Promise<WebRTCPeerConnection> createPeerConnection(WebRTCIceServer[] webRTCIceServers, WebRTCSettings settings) {
        return null;
    }

    @NotNull
    @Override
    public Promise<WebRTCMediaStream> getUserAudio() {
        return null;
    }

    @Override
    public boolean supportsPreConnections() {
        return false;
    }
}
