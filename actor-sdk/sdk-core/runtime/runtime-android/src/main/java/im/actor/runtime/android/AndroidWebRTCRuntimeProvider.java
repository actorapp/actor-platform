package im.actor.runtime.android;

import org.jetbrains.annotations.NotNull;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;

import im.actor.runtime.WebRTCRuntime;
import im.actor.runtime.actors.ActorContext;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseFunc;
import im.actor.runtime.promise.PromiseResolver;
import im.actor.runtime.android.webrtc.AndroidMediaStream;
import im.actor.runtime.android.webrtc.AndroidPeerConnection;
import im.actor.runtime.webrtc.WebRTCIceServer;
import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCPeerConnection;
import im.actor.runtime.webrtc.WebRTCSettings;

public class AndroidWebRTCRuntimeProvider implements WebRTCRuntime {

    public static final PeerConnectionFactory FACTORY;

    static {
        PeerConnectionFactory.initializeAndroidGlobals(AndroidContext.getContext(), true, true, true);
        FACTORY = new PeerConnectionFactory();
    }

    @NotNull
    @Override
    public Promise<WebRTCPeerConnection> createPeerConnection(final WebRTCIceServer[] webRTCIceServers, final WebRTCSettings settings) {
        return new Promise<>(new PromiseFunc<WebRTCPeerConnection>() {
            @Override
            public void exec(@NotNull PromiseResolver<WebRTCPeerConnection> resolver) {
                resolver.result(new AndroidPeerConnection(webRTCIceServers, settings));
            }
        });
    }

    @Override
    @NotNull
    public Promise<WebRTCMediaStream> getUserAudio() {
        return new Promise<>(new PromiseFunc<WebRTCMediaStream>() {
            @Override
            public void exec(@NotNull final PromiseResolver<WebRTCMediaStream> resolver) {
                resolver.result(new AndroidMediaStream(FACTORY.createLocalMediaStream("ARDAMS"), true, true));
            }
        });
    }
}