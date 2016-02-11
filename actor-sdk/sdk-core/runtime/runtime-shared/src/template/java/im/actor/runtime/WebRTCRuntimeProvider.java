package im.actor.runtime;

import im.actor.runtime.webrtc.WebRTCPeerConnection;

public class WebRTCRuntimeProvider implements WebRTCRuntime {
    @Override
    public WebRTCPeerConnection createPeerConnection() {
        throw new RuntimeException("Dumb");
    }
}
