package im.actor.runtime;

import im.actor.runtime.promise.Promise;
import im.actor.runtime.webrtc.WebRTCPeerConnection;

/**
 * Web RTC support runtime
 */
public interface WebRTCRuntime {

    /**
     * Creating of WebRTC peer connection
     *
     * @return created peer connection
     */
    Promise<WebRTCPeerConnection> createPeerConnection();
}
