package im.actor.runtime;

import im.actor.runtime.promise.Promise;
import im.actor.runtime.webrtc.WebRTCIceServer;
import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCPeerConnection;
import im.actor.runtime.webrtc.WebRTCSettings;

public final class WebRTC {

    private static WebRTCRuntime rtcRuntime = new WebRTCRuntimeProvider();

    public static Promise<WebRTCPeerConnection> createPeerConnection(WebRTCIceServer[] iceServers,
                                                                     WebRTCSettings webRTCSettings) {
        return rtcRuntime.createPeerConnection(iceServers, webRTCSettings);
    }

    public static Promise<WebRTCMediaStream> getUserMedia(boolean isVideoEnabled) {
        return rtcRuntime.getUserMedia(isVideoEnabled);
    }

    public static boolean isSupportsPreConnections() {
        return rtcRuntime.supportsPreConnections();
    }
}
