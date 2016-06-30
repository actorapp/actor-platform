package im.actor.runtime.android.webrtc;

import org.webrtc.AudioTrack;
import org.webrtc.MediaStream;
import org.webrtc.VideoTrack;

import im.actor.runtime.android.AndroidWebRTCRuntimeProvider;
import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCMediaTrack;

public class AndroidMediaStream implements WebRTCMediaStream {

    private final MediaStream stream;

    private final boolean isLocal;
    private AndroidVideoSource videoSource;
    private AndroidAudioSource audioSource;
    private AudioTrack localAudioTrack;
    private VideoTrack localVideoTrack;
    private final WebRTCMediaTrack[] videoTracks;
    private final WebRTCMediaTrack[] audioTracks;
    private final WebRTCMediaTrack[] allTracks;

    public AndroidMediaStream(MediaStream stream) {
        this.isLocal = false;
        this.stream = stream;
        this.allTracks = new WebRTCMediaTrack[stream.audioTracks.size() + stream.videoTracks.size()];
        this.audioTracks = new WebRTCMediaTrack[stream.audioTracks.size()];
        for (int i = 0; i < this.audioTracks.length; i++) {
            audioTracks[i] = new AndroidAudioTrack(stream.audioTracks.get(i), this);
            allTracks[i] = audioTracks[i];
        }
        this.videoTracks = new WebRTCMediaTrack[stream.videoTracks.size()];
        for (int i = 0; i < this.videoTracks.length; i++) {
            videoTracks[i] = new AndroidVideoTrack(stream.videoTracks.get(i), this);
            allTracks[audioTracks.length + i] = videoTracks[i];
        }
    }

    public AndroidMediaStream(AndroidAudioSource audioSource, AndroidVideoSource videoSource) {
        this.isLocal = true;
        this.videoSource = videoSource;
        this.audioSource = audioSource;
        this.stream = AndroidWebRTCRuntimeProvider.FACTORY.createLocalMediaStream("ARDAMSv0");
        if (audioSource != null) {
            localAudioTrack = AndroidWebRTCRuntimeProvider.FACTORY.createAudioTrack("audio0", audioSource.getAudioSource());
            stream.addTrack(localAudioTrack);
            audioTracks = new WebRTCMediaTrack[]{new AndroidAudioTrack(localAudioTrack, this)};
        } else {
            audioTracks = new WebRTCMediaTrack[0];
        }
        if (videoSource != null) {
            localVideoTrack = AndroidWebRTCRuntimeProvider.FACTORY.createVideoTrack("video0", videoSource.getVideoSource());
            stream.addPreservedTrack(localVideoTrack);
            videoTracks = new WebRTCMediaTrack[]{new AndroidVideoTrack(localVideoTrack, this)};
        } else {
            videoTracks = new WebRTCMediaTrack[0];
        }
        this.allTracks = new WebRTCMediaTrack[audioTracks.length + videoTracks.length];
        for (int i = 0; i < audioTracks.length; i++) {
            allTracks[i] = audioTracks[i];
        }
        for (int i = 0; i < videoTracks.length; i++) {
            allTracks[audioTracks.length + i] = videoTracks[i];
        }
    }

    public MediaStream getStream() {
        return stream;
    }

    @Override
    public WebRTCMediaTrack[] getAudioTracks() {
        return audioTracks;
    }

    @Override
    public WebRTCMediaTrack[] getVideoTracks() {
        return videoTracks;
    }

    @Override
    public WebRTCMediaTrack[] getTracks() {
        return allTracks;
    }

    @Override
    public void close() {

        if (isLocal) {
            if (localAudioTrack != null) {
                stream.removeTrack(localAudioTrack);
                localAudioTrack.dispose();
                localAudioTrack = null;
            }

            if (localVideoTrack != null) {
                stream.removeTrack(localVideoTrack);
                localVideoTrack.dispose();
                localVideoTrack = null;
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
}
