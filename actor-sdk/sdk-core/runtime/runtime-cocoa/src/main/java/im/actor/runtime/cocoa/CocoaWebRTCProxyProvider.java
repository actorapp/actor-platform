package im.actor.runtime.cocoa;

import com.google.j2objc.annotations.ObjectiveCName;

import org.jetbrains.annotations.NotNull;

import im.actor.runtime.WebRTCRuntime;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.Promises;
import im.actor.runtime.webrtc.WebRTCIceServer;
import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCPeerConnection;
import im.actor.runtime.webrtc.WebRTCSettings;

public class CocoaWebRTCProxyProvider implements WebRTCRuntime {

    private static WebRTCRuntime rtcRuntime;

    @ObjectiveCName("setWebRTCRuntime:")
    public static void setWebRTCRuntime(WebRTCRuntime rtcRuntime) {
        CocoaWebRTCProxyProvider.rtcRuntime = rtcRuntime;
    }

    @NotNull
    @Override
    public Promise<WebRTCPeerConnection> createPeerConnection(WebRTCIceServer[] webRTCIceServers, WebRTCSettings settings) {
        if (rtcRuntime == null) {
            return Promise.failure(new RuntimeException("WebRTC Runtime not set"));
        }
        return rtcRuntime.createPeerConnection(webRTCIceServers, settings);
    }

    @NotNull
    @Override
    public Promise<WebRTCMediaStream> getUserMedia(boolean isVideoEnabled) {
        if (rtcRuntime == null) {
            return Promise.failure(new RuntimeException("WebRTC Runtime not set"));
        }
        return rtcRuntime.getUserMedia(isVideoEnabled);
    }

    @Override
    public boolean supportsPreConnections() {
        if (rtcRuntime == null) {
            throw new RuntimeException("WebRTC Runtime not set");
        }
        return rtcRuntime.supportsPreConnections();
    }
}
