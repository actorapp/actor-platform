package im.actor.runtime.js;

import org.jetbrains.annotations.NotNull;

import im.actor.core.js.modules.JsScheduller;
import im.actor.runtime.WebRTCRuntime;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.js.webrtc.JsMediaStream;
import im.actor.runtime.js.webrtc.JsPeerConnection;
import im.actor.runtime.js.webrtc.JsStreaming;
import im.actor.runtime.js.webrtc.PeerConnection;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseFunc;
import im.actor.runtime.promise.PromiseResolver;
import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCPeerConnection;

public class JsWebRTCProvider implements WebRTCRuntime {

    @NotNull
    @Override
    public Promise<WebRTCPeerConnection> createPeerConnection() {
        return new Promise<>(new PromiseFunc<WebRTCPeerConnection>() {
            @Override
            public void exec(@NotNull PromiseResolver<WebRTCPeerConnection> resolver) {
                resolver.result(new PeerConnection(JsPeerConnection.create(null)));
            }
        });
    }

    @NotNull
    @Override
    public Promise<WebRTCMediaStream> getUserAudio() {
        return new Promise<>(new PromiseFunc<WebRTCMediaStream>() {
            @Override
            public void exec(@NotNull final PromiseResolver<WebRTCMediaStream> resolver) {
                JsStreaming.getUserAudio().then(new Consumer<JsMediaStream>() {
                    @Override
                    public void apply(JsMediaStream jsMediaStream) {
                        resolver.result(jsMediaStream);
                    }
                }).failure(new Consumer<Exception>() {
                    @Override
                    public void apply(Exception e) {
                        resolver.error(e);
                    }
                }).done(JsScheduller.scheduller());
            }
        });
    }
}
