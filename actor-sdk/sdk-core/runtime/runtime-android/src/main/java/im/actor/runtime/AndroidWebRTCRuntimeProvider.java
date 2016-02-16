package im.actor.runtime;

import org.jetbrains.annotations.NotNull;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;

import java.util.ArrayList;

import im.actor.runtime.function.Consumer;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseFunc;
import im.actor.runtime.promise.PromiseResolver;
import im.actor.runtime.promise.Promises;
import im.actor.runtime.webrtc.AndroidMediaStream;
import im.actor.runtime.webrtc.AndroidPeerConnection;
import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCPeerConnection;
import im.actor.sdk.ActorSDK;

public class AndroidWebRTCRuntimeProvider implements WebRTCRuntime {
    private static PeerConnectionFactory factory;
    public AndroidWebRTCRuntimeProvider() {
        PeerConnectionFactory.initializeAndroidGlobals(ActorSDK.sharedActor().getMessenger().getContext(), true, true, true);
        factory = new PeerConnectionFactory();
    }

    @Override
    @NotNull
    public Promise<WebRTCPeerConnection> createPeerConnection() {
        return new Promise<WebRTCPeerConnection>(new PromiseFunc<WebRTCPeerConnection>() {
            @Override
            public void exec(@NotNull PromiseResolver<WebRTCPeerConnection> resolver) {

                resolver.result(new AndroidPeerConnection());
            }
        });
    }

    @Override
    @NotNull
    public Promise<WebRTCMediaStream> getUserAudio() {
        return new Promise<WebRTCMediaStream>(new PromiseFunc<WebRTCMediaStream>() {
            @Override
            public void exec(@NotNull final PromiseResolver<WebRTCMediaStream> resolver) {
                resolver.result(new AndroidMediaStream(factory.createLocalMediaStream("ARDAMS"), true, true));
            }
        });
    }

    public static PeerConnectionFactory factory(){
        return factory;
    }
}
