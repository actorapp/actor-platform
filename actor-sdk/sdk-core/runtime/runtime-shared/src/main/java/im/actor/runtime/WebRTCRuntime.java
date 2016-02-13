package im.actor.runtime;

import com.google.j2objc.annotations.ObjectiveCName;

import org.jetbrains.annotations.NotNull;

import im.actor.runtime.promise.Promise;
import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCPeerConnection;

/**
 * Web RTC support runtime
 */
public interface WebRTCRuntime {

    /**
     * Creating of WebRTC peer connection
     *
     * @return promise of peer connection
     */
    @NotNull
    @ObjectiveCName("createPeerConnection")
    Promise<WebRTCPeerConnection> createPeerConnection();

    /**
     * Getting User Audio stream
     *
     * @return promise of audio stream
     */
    @NotNull
    @ObjectiveCName("getUserAudio")
    Promise<WebRTCMediaStream> getUserAudio();
}
