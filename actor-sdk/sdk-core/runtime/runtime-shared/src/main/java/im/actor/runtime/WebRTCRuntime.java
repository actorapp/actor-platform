package im.actor.runtime;

import com.google.j2objc.annotations.ObjectiveCName;

import org.jetbrains.annotations.NotNull;

import im.actor.runtime.promise.Promise;
import im.actor.runtime.webrtc.WebRTCIceServer;
import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCPeerConnection;
import im.actor.runtime.webrtc.WebRTCSettings;

/**
 * Web RTC support runtime
 */
public interface WebRTCRuntime {

    /**
     * Creating of WebRTC peer connection
     *
     * @param webRTCIceServers servers for peer connection
     * @param settings         settings for peer connection
     * @return promise of peer connection
     */
    @NotNull
    @ObjectiveCName("createPeerConnectionWithServers:withSettings:")
    Promise<WebRTCPeerConnection> createPeerConnection(WebRTCIceServer[] webRTCIceServers,
                                                       WebRTCSettings settings);

    /**
     * Getting User Audio stream
     *
     * @return promise of audio stream
     */
    @NotNull
    @ObjectiveCName("getUserMediaWithIsVideoEnabled:")
    Promise<WebRTCMediaStream> getUserMedia(boolean isVideoEnabled);

    /**
     * Return if implementation supports pre-connection technique
     *
     * @return true if preconnections are supported
     */
    @ObjectiveCName("supportsPreConnections")
    boolean supportsPreConnections();
}
