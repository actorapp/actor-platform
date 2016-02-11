package im.actor.runtime;

import im.actor.runtime.promise.Promise;
import im.actor.runtime.webrtc.WebRTCLocalStream;
import im.actor.runtime.webrtc.WebRTCPeerConnection;

public final class WebRTC {

    private static WebRTCRuntime rtcRuntime = new WebRTCRuntimeProvider();

    public static Promise<WebRTCPeerConnection> createPeerConnection() {
        return rtcRuntime.createPeerConnection();
    }

    public static Promise<WebRTCLocalStream> getUserAudio() {
        return rtcRuntime.getUserAudio();
    }
}
