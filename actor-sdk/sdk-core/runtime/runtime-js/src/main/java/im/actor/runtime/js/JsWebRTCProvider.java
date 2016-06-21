package im.actor.runtime.js;

import com.google.gwt.core.client.JsArray;

import org.jetbrains.annotations.NotNull;

import im.actor.runtime.WebRTCRuntime;
import im.actor.runtime.js.webrtc.js.JsIceServer;
import im.actor.runtime.js.webrtc.js.JsPeerConnection;
import im.actor.runtime.js.webrtc.js.JsPeerConnectionConfig;
import im.actor.runtime.js.webrtc.js.JsStreaming;
import im.actor.runtime.js.webrtc.MediaStream;
import im.actor.runtime.js.webrtc.PeerConnection;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseFunc;
import im.actor.runtime.webrtc.WebRTCIceServer;
import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCPeerConnection;
import im.actor.runtime.webrtc.WebRTCSettings;

public class JsWebRTCProvider implements WebRTCRuntime {

    @NotNull
    @Override
    public Promise<WebRTCPeerConnection> createPeerConnection(final WebRTCIceServer[] webRTCIceServers, WebRTCSettings settings) {
        return new Promise<>((PromiseFunc<WebRTCPeerConnection>) resolver -> {
            JsArray<JsIceServer> servers = JsArray.createArray().cast();
            for (WebRTCIceServer s : webRTCIceServers) {
                if (s.getUsername() != null || s.getCredential() != null) {
                    servers.push(JsIceServer.create(s.getUrl(), s.getUsername(), s.getCredential()));
                } else {
                    servers.push(JsIceServer.create(s.getUrl()));
                }
            }
            resolver.result(new PeerConnection(JsPeerConnection.create(JsPeerConnectionConfig.create(servers))));
        });
    }

    @NotNull
    @Override
    public Promise<WebRTCMediaStream> getUserMedia(boolean isAudioEnabled, boolean isVideoEnabled) {
        return JsStreaming.getUserMedia(isAudioEnabled, isVideoEnabled)
                .map((jsMediaStream -> new MediaStream(jsMediaStream, false)));
    }

    @Override
    public boolean supportsPreConnections() {
        return true;
    }
}
