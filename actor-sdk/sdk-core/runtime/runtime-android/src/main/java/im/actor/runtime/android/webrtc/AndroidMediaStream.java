package im.actor.runtime.android.webrtc;

import org.webrtc.AudioTrack;
import org.webrtc.MediaStream;
import org.webrtc.VideoTrack;

import im.actor.runtime.android.AndroidWebRTCRuntimeProvider;
import im.actor.runtime.webrtc.WebRTCMediaStream;

public class AndroidMediaStream implements WebRTCMediaStream {

    private MediaStream stream;

    private boolean isLocal;
    private AndroidVideoSource videoSource;
    private VideoTrack videoTrack;
    private AndroidAudioSource audioSource;
    private AudioTrack audioTrack;

    public AndroidMediaStream(MediaStream stream) {
        this.isLocal = false;
        this.stream = stream;
    }

    public AndroidMediaStream(AndroidAudioSource audioSource, AndroidVideoSource videoSource) {
        this.isLocal = true;
        this.videoSource = videoSource;
        this.audioSource = audioSource;
        this.stream = AndroidWebRTCRuntimeProvider.FACTORY.createLocalMediaStream("ARDAMSv0");
        if (audioSource != null) {
            this.audioTrack = AndroidWebRTCRuntimeProvider.FACTORY.createAudioTrack("audio0", audioSource.getAudioSource());
            this.stream.addTrack(audioTrack);
        }
        if (videoSource != null) {
            this.videoTrack = AndroidWebRTCRuntimeProvider.FACTORY.createVideoTrack("video0", videoSource.getVideoSource());
            this.stream.addPreservedTrack(videoTrack);
        }
    }

    @Override
    public void close() {

        if (isLocal) {
            if (audioTrack != null) {
                stream.removeTrack(audioTrack);
                audioTrack.dispose();
                audioTrack = null;
            }

            if (videoTrack != null) {
                stream.removeTrack(videoTrack);
                videoTrack.dispose();
                videoTrack = null;
            }

            //
            // I Have No idea why releasing of video/audio sources need to be here
            // It is looks almost like "stream.dispose();" implementation, but
            // before freeing stream we are releasing sources. But we already removed any track?
            // Unlinking before track removing also produce crashes.
            //
            if (audioSource != null) {
                audioSource.unlink();
                audioSource = null;
            }
            if (videoSource != null) {
                videoSource.unlink();
                videoSource = null;
            }
        }

        stream.dispose();
    }

    public MediaStream getStream() {
        return stream;
    }
}
