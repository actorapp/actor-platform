package im.actor.runtime.android.webrtc;

import org.webrtc.AudioTrack;

import im.actor.runtime.webrtc.WebRTCMediaTrack;
import im.actor.runtime.webrtc.WebRTCTrackType;

public class AndroidAudioTrack implements WebRTCMediaTrack {

    private AudioTrack audioTrack;

    public AndroidAudioTrack(AudioTrack audioTrack) {
        this.audioTrack = audioTrack;
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
        audioTrack.setEnabled(isEnabled);
    }
}
