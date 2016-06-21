package im.actor.runtime.android;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.support.annotation.NonNull;

import org.jetbrains.annotations.NotNull;
import org.webrtc.AudioSource;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import im.actor.runtime.WebRTCRuntime;
import im.actor.runtime.actors.ActorContext;
import im.actor.runtime.android.webrtc.AndroidAudioSource;
import im.actor.runtime.android.webrtc.AndroidVideoSource;
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
    private static final Handler sVcHandler;

    static {
        PeerConnectionFactory.initializeAndroidGlobals(AndroidContext.getContext(), true, true, true);
        FACTORY = new PeerConnectionFactory();
        HandlerThread vcthread = new HandlerThread("PeerConnectionConnectionThread");
        vcthread.start();
        sVcHandler = new Handler(vcthread.getLooper());
    }


    @NotNull
    @Override
    public Promise<WebRTCPeerConnection> createPeerConnection(final WebRTCIceServer[] webRTCIceServers, final WebRTCSettings settings) {
        return new Promise<>((PromiseFunc<WebRTCPeerConnection>) resolver -> {
            resolver.result(new AndroidPeerConnection(webRTCIceServers, settings));
        });
    }

    @NotNull
    @Override
    public Promise<WebRTCMediaStream> getUserMedia(boolean isAudioEnabled, boolean isVideoEnabled) {
        return new Promise<>((PromiseFunc<WebRTCMediaStream>) resolver -> {
            sVcHandler.post(() -> {
                AndroidAudioSource audioSource = null;
                if (isAudioEnabled) {
                    audioSource = AndroidAudioSource.pickAudioSource();
                }
                AndroidVideoSource videoSource = null;
                if (isVideoEnabled) {
                    videoSource = AndroidVideoSource.pickVideoSource();
                }
                resolver.result(new AndroidMediaStream(audioSource, videoSource));
            });
        });
    }

    @Override
    public boolean supportsPreConnections() {
        return false;
    }

    public static void postToHandler(Runnable r) {
        sVcHandler.post(r);
    }
}