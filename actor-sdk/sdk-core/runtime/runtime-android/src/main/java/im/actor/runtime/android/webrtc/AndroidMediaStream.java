package im.actor.runtime.android.webrtc;

import android.media.MediaRecorder;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import im.actor.runtime.android.AndroidWebRTCRuntimeProvider;
import im.actor.runtime.webrtc.WebRTCMediaStream;

public class AndroidMediaStream implements WebRTCMediaStream {

    private AudioTrack audioTrack;
    private VideoTrack videoTrack;
    private MediaStream stream;
    private boolean isEnabled = false;
    private boolean local;
    private boolean diposed = false;
    private VideoSource videoSource;

    public AndroidMediaStream(MediaStream stream) {
        this(stream, true, false);
    }

    public AndroidMediaStream(final MediaStream stream, boolean autoPlay, boolean local) {
        this.local = local;
        this.stream = stream;
        if (!local) {
            audioTrack = stream.audioTracks.get(0);
            try {
                videoTrack = stream.videoTracks.get(0);
            } catch (Exception e) {
                //Ignore
            }
        } else {
            AndroidWebRTCRuntimeProvider.postToHandler(new Runnable() {
                @Override
                public void run() {
                    MediaConstraints audioConstarints = new MediaConstraints();
                    audioConstarints.mandatory.add(new MediaConstraints.KeyValuePair("googNoiseSuppression", "true"));
                    audioConstarints.mandatory.add(new MediaConstraints.KeyValuePair("googEchoCancellation", "true"));
                    AudioSource audioSource = AndroidWebRTCRuntimeProvider.FACTORY.createAudioSource(audioConstarints);
                    videoSource = AndroidWebRTCRuntimeProvider.FACTORY.createVideoSource(getVideoCapturer(), new MediaConstraints());
                    videoTrack = AndroidWebRTCRuntimeProvider.FACTORY.createVideoTrack("ARDAMSv0", videoSource);
                    audioTrack = AndroidWebRTCRuntimeProvider.FACTORY.createAudioTrack("ARDAMSa0", audioSource);
                    stream.addTrack(audioTrack);
                    stream.addTrack(videoTrack);
                }
            });

        }
        setEnabled(autoPlay);
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public void setEnabled(boolean isEnabled) {
        if (audioTrack != null) {
            audioTrack.setEnabled(isEnabled);
            this.isEnabled = isEnabled;
        }

    }

    @Override
    public void close() {
        stream.dispose();
    }

    public MediaStream getStream() {
        return stream;
    }

    public boolean isLocal() {
        return local;
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

    public AudioTrack getAudioTrack() {
        return audioTrack;
    }

    public VideoTrack getVideoTrack() {
        return videoTrack;
    }

    public VideoSource getVideoSource() {
        return videoSource;
    }

    public void disposeVideo() {
        if (videoSource != null) {
            videoSource.dispose();
        }
    }
}
