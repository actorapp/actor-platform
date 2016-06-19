package im.actor.runtime.android;

import android.os.Handler;
import android.os.HandlerThread;
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
    private static final Object sVcLock = new Object();
    private static Handler sVcHandler = null;

    static {
        PeerConnectionFactory.initializeAndroidGlobals(AndroidContext.getContext(), true, true, true);
        FACTORY = new PeerConnectionFactory();
        synchronized (sVcLock) {
            if (sVcHandler == null) {
                HandlerThread vcthread = new HandlerThread("PeerConnectionConnectionThread");
                vcthread.start();
                sVcHandler = new Handler(vcthread.getLooper());
            }
        }
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

                MediaStream stream = FACTORY.createLocalMediaStream("ARDAMS");

                if (isAudioEnabled) {
                    MediaConstraints audioConstraints = new MediaConstraints();
                    audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googNoiseSuppression", "true"));
                    audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googEchoCancellation", "true"));
                    AudioSource audioSource = FACTORY.createAudioSource(audioConstraints);
                    stream.addTrack(FACTORY.createAudioTrack("audio0", audioSource));
                }

                if (isVideoEnabled) {
                    VideoSource videoSource = AndroidWebRTCRuntimeProvider.FACTORY.createVideoSource(getVideoCapturer(), new MediaConstraints());
                    VideoTrack videoTrack = AndroidWebRTCRuntimeProvider.FACTORY.createVideoTrack("video0", videoSource);
                    stream.addTrack(videoTrack);
                }

                resolver.result(new AndroidMediaStream(stream));
            });
        });
    }

    @Override
    public boolean supportsPreConnections() {
        return false;
    }

    // Cycle through likely device names for the camera and return the first
    // capturer that works, or crash if none do.
    private VideoCapturer getVideoCapturer() {
        String[] cameraFacing = {"front", "back"};
        int[] cameraIndex = {0, 1};
        int[] cameraOrientation = {0, 90, 180, 270};
        for (String facing : cameraFacing) {
            for (int index : cameraIndex) {
                for (int orientation : cameraOrientation) {
                    String name = "Camera " + index + ", Facing " + facing +
                            ", Orientation " + orientation;
                    VideoCapturer capturer = VideoCapturer.create(name);
                    if (capturer != null) {
                        return capturer;
                    }
                }
            }
        }
        throw new RuntimeException("Failed to open capturer");
    }

    public static void postToHandler(Runnable r) {
        sVcHandler.post(r);
    }
}