package im.actor.runtime;

import im.actor.runtime.promise.Promise;
import im.actor.runtime.webrtc.WebRTCPeerConnection;

public class WebRTCRuntimeProvider implements WebRTCRuntime {

    @Override
    public Promise<WebRTCPeerConnection> createPeerConnection() {
        return null;
    }
}
