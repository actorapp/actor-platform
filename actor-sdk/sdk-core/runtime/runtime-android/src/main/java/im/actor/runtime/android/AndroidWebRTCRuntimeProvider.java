package im.actor.runtime.android;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;

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
    private static Object sVcLock = new Object();
    private static Handler sVcHandler = null;
    private static PeerConnectionCallback connectionCallback;
    private static AndroidPeerConnection currentPeerConnection;

    static {
        PeerConnectionFactory.initializeAndroidGlobals(AndroidContext.getContext(), true, true, true);
        FACTORY = new PeerConnectionFactory();
        synchronized (sVcLock) {
            if (sVcHandler == null) {
                HandlerThread vcthread = new HandlerThread(
                        "PeerConnectionConnectionThread");
                vcthread.start();
                sVcHandler = new Handler(vcthread.getLooper());
            }
        }
    }


    @NotNull
    @Override
    public Promise<WebRTCPeerConnection> createPeerConnection(final WebRTCIceServer[] webRTCIceServers, final WebRTCSettings settings) {
        return new Promise<>(new PromiseFunc<WebRTCPeerConnection>() {
            @Override
            public void exec(@NonNull @NotNull final PromiseResolver<WebRTCPeerConnection> resolver) {
                currentPeerConnection = new AndroidPeerConnection(webRTCIceServers, settings);
                resolver.result(currentPeerConnection);
                if (connectionCallback != null) {
                    connectionCallback.onPeerConncetionAvailable(currentPeerConnection);
                    connectionCallback = null;
                }

            }
        });
    }

    @Override
    @NotNull
    public Promise<WebRTCMediaStream> getUserAudio() {
        return new Promise<>(new PromiseFunc<WebRTCMediaStream>() {
            @Override
            public void exec(@NonNull @NotNull final PromiseResolver<WebRTCMediaStream> resolver) {
                sVcHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        resolver.result(new AndroidMediaStream(FACTORY.createLocalMediaStream("ARDAMS"), true, true));
                    }
                });
            }
        });
    }

    @Override
    public boolean supportsPreConnections() {
        return true;
    }

    public static void postToHandler(Runnable r) {
        sVcHandler.post(r);
    }

    public AndroidPeerConnection getCurrentPeerConnection() {
        return currentPeerConnection;
    }

    public static void bindPeerConnection(PeerConnectionCallback callback) {
        if (currentPeerConnection != null) {
            callback.onPeerConncetionAvailable(currentPeerConnection);
        } else {
            connectionCallback = callback;
        }
    }

    public static void unbindPeerConnection() {
        if (currentPeerConnection != null) {
            currentPeerConnection.unbind();
        }
        currentPeerConnection = null;
    }

    public interface PeerConnectionCallback {
        void onPeerConncetionAvailable(AndroidPeerConnection peerConnection);
    }
}