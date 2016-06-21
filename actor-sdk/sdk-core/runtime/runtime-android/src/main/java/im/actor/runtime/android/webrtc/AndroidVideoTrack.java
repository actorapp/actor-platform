package im.actor.runtime.android.webrtc;

import org.webrtc.VideoTrack;

import im.actor.runtime.webrtc.WebRTCMediaTrack;
import im.actor.runtime.webrtc.WebRTCTrackType;

public class AndroidVideoTrack implements WebRTCMediaTrack {

    private VideoTrack videoTrack;

    public AndroidVideoTrack(VideoTrack videoTrack) {
        this.videoTrack = videoTrack;
    }

    public VideoTrack getVideoTrack() {
        return videoTrack;
    }

    @Override
    public int getTrackType() {
        return WebRTCTrackType.VIDEO;
    }

    @Override
    public boolean isEnabled() {
        return videoTrack.enabled();
    }

    @Override
    public void setEnabled(boolean isEnabled) {
        videoTrack.setEnabled(isEnabled);
    }
}
