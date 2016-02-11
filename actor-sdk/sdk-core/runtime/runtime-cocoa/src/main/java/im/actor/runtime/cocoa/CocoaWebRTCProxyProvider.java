package im.actor.runtime.cocoa;

import com.google.j2objc.annotations.ObjectiveCName;

import im.actor.runtime.WebRTCRuntime;
import im.actor.runtime.webrtc.WebRTCPeerConnection;

public class CocoaWebRTCProxyProvider implements WebRTCRuntime {

    private static WebRTCRuntime rtcRuntime;

    @ObjectiveCName("setWebRTCRuntime:")
    public static void setWebRTCRuntime(WebRTCRuntime rtcRuntime) {
        CocoaWebRTCProxyProvider.rtcRuntime = rtcRuntime;
    }

    @Override
    public WebRTCPeerConnection createPeerConnection() {
        if (rtcRuntime == null) {
            throw new RuntimeException("Storage Runtime not set");
        }
        return rtcRuntime.createPeerConnection();
    }
}
