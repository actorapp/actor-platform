package im.actor.runtime.cocoa;

import com.google.j2objc.annotations.ObjectiveCName;

import org.jetbrains.annotations.NotNull;

import im.actor.runtime.WebRTCRuntime;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.Promises;
import im.actor.runtime.webrtc.WebRTCLocalStream;
import im.actor.runtime.webrtc.WebRTCPeerConnection;

public class CocoaWebRTCProxyProvider implements WebRTCRuntime {

    private static WebRTCRuntime rtcRuntime;

    @ObjectiveCName("setWebRTCRuntime:")
    public static void setWebRTCRuntime(WebRTCRuntime rtcRuntime) {
        CocoaWebRTCProxyProvider.rtcRuntime = rtcRuntime;
    }

    @Override
    @NotNull
    public Promise<WebRTCPeerConnection> createPeerConnection() {
        if (rtcRuntime == null) {
            return Promises.failure(new RuntimeException("WebRTC Runtime not set"));
        }
        return rtcRuntime.createPeerConnection();
    }

    @NotNull
    @Override
    public Promise<WebRTCLocalStream> getUserAudio() {
        if (rtcRuntime == null) {
            return Promises.failure(new RuntimeException("WebRTC Runtime not set"));
        }
        return rtcRuntime.getUserAudio();
    }
}
