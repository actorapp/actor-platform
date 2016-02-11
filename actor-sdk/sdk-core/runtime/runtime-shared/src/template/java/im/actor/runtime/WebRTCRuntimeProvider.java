package im.actor.runtime;

import org.jetbrains.annotations.NotNull;

import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.Promises;
import im.actor.runtime.webrtc.WebRTCLocalStream;
import im.actor.runtime.webrtc.WebRTCPeerConnection;

public class WebRTCRuntimeProvider implements WebRTCRuntime {

    @Override
    @NotNull
    public Promise<WebRTCPeerConnection> createPeerConnection() {
        return Promises.failure(new RuntimeException("Dumb"));
    }

    @Override
    @NotNull
    public Promise<WebRTCLocalStream> getUserAudio() {
        return Promises.failure(new RuntimeException("Dumb"));
    }
}
