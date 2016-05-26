package im.actor.runtime.js;

import com.google.gwt.core.client.JsArray;

import org.jetbrains.annotations.NotNull;

import im.actor.runtime.WebRTCRuntime;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.js.threading.JsScheduller;
import im.actor.runtime.js.webrtc.JsIceServer;
import im.actor.runtime.js.webrtc.JsMediaStream;
import im.actor.runtime.js.webrtc.JsPeerConnection;
import im.actor.runtime.js.webrtc.JsPeerConnectionConfig;
import im.actor.runtime.js.webrtc.JsStreaming;
import im.actor.runtime.js.webrtc.MediaStream;
import im.actor.runtime.js.webrtc.PeerConnection;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseFunc;
import im.actor.runtime.promise.PromiseResolver;
import im.actor.runtime.webrtc.WebRTCIceServer;
import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCPeerConnection;
import im.actor.runtime.webrtc.WebRTCSettings;

public class JsWebRTCProvider implements WebRTCRuntime {

    @NotNull
    @Override
    public Promise<WebRTCPeerConnection> createPeerConnection(final WebRTCIceServer[] webRTCIceServers, WebRTCSettings settings) {
        return new Promise<>(new PromiseFunc<WebRTCPeerConnection>() {
            @Override
            public void exec(@NotNull PromiseResolver<WebRTCPeerConnection> resolver) {
                JsArray<JsIceServer> servers = JsArray.createArray().cast();
                for (WebRTCIceServer s : webRTCIceServers) {
                    if (s.getUsername() != null || s.getCredential() != null) {
                        servers.push(JsIceServer.create(s.getUrl(), s.getUsername(), s.getCredential()));
                    } else {
                        servers.push(JsIceServer.create(s.getUrl()));
                    }
                }
                resolver.result(new PeerConnection(JsPeerConnection.create(JsPeerConnectionConfig.create(servers))));
            }
        });
    }

    @NotNull
    @Override
    public Promise<WebRTCMediaStream> getUserMedia(boolean isVideoEnabled) {
        return new Promise<>(new PromiseFunc<WebRTCMediaStream>() {
            @Override
            public void exec(@NotNull final PromiseResolver<WebRTCMediaStream> resolver) {
                JsStreaming.getUserAudio().then(new Consumer<JsMediaStream>() {
                    @Override
                    public void apply(JsMediaStream jsMediaStream) {
                        resolver.result(new MediaStream(jsMediaStream, false));
                    }
                }).failure(new Consumer<Exception>() {
                    @Override
                    public void apply(Exception e) {
                        resolver.error(e);
                    }
                });
            }
        });
    }

    @Override
    public boolean supportsPreConnections() {
        return true;
    }
}
