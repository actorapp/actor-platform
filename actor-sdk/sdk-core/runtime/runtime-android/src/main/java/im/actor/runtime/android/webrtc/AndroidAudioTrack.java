package im.actor.runtime.android.webrtc;

import org.webrtc.AudioTrack;

import im.actor.runtime.webrtc.WebRTCMediaTrack;
import im.actor.runtime.webrtc.WebRTCTrackType;

public class AndroidAudioTrack implements WebRTCMediaTrack {

    private AudioTrack audioTrack;
    private AndroidMediaStream stream;

    public AndroidAudioTrack(AudioTrack audioTrack, AndroidMediaStream stream) {
        this.audioTrack = audioTrack;
        this.stream = stream;
    }

    public AudioTrack getAudioTrack() {
        return audioTrack;
    }

    @Override
    public int getTrackType() {
        return WebRTCTrackType.AUDIO;
    }

    @Override
    public boolean isEnabled() {
        return audioTrack.enabled();
    }

    @Override
    public void setEnabled(boolean isEnabled) {
        if (stream.getStream().audioTracks.contains(audioTrack)) {
            audioTrack.setEnabled(isEnabled);
        }
    }
}
