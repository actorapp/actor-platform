package im.actor.runtime.android.webrtc;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;

import im.actor.runtime.android.AndroidWebRTCRuntimeProvider;
import im.actor.runtime.webrtc.WebRTCMediaStream;

public class AndroidMediaStream implements WebRTCMediaStream {

    private AudioTrack audioTrack;
    private MediaStream stream;
    private boolean isEnabled = false;
    private boolean local;
    private boolean diposed = false;

    public AndroidMediaStream(MediaStream stream) {
        this(stream, true, false);
    }

    public AndroidMediaStream(final MediaStream stream, boolean autoPlay, boolean local) {
        this.local = local;
        this.stream = stream;
        if (!local) {
            audioTrack = stream.audioTracks.get(0);
        } else {
            AndroidWebRTCRuntimeProvider.postToHandler(new Runnable() {
                @Override
                public void run() {
                    MediaConstraints audioConstarints = new MediaConstraints();
                    audioConstarints.mandatory.add(new MediaConstraints.KeyValuePair("googNoiseSuppression", "true"));
                    audioConstarints.mandatory.add(new MediaConstraints.KeyValuePair("googEchoCancellation", "true"));
                    AudioSource audioSource = AndroidWebRTCRuntimeProvider.FACTORY.createAudioSource(audioConstarints);
                    audioTrack = AndroidWebRTCRuntimeProvider.FACTORY.createAudioTrack("ARDAMSa0", audioSource);
                    stream.addTrack(audioTrack);
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
}
