package im.actor.runtime.se;

import org.jetbrains.annotations.NotNull;

import im.actor.runtime.WebRTCRuntime;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCPeerConnection;

public class JavaSeWebRTCProvider implements WebRTCRuntime {

    @NotNull
    @Override
    public Promise<WebRTCPeerConnection> createPeerConnection() {
        return null;
    }

    @NotNull
    @Override
    public Promise<WebRTCMediaStream> getUserAudio() {
        return null;
    }
}
