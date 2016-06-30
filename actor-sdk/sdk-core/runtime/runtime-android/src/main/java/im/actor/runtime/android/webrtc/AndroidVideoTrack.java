package im.actor.runtime.android.webrtc;

import org.webrtc.VideoTrack;

import im.actor.runtime.webrtc.WebRTCMediaTrack;
import im.actor.runtime.webrtc.WebRTCTrackType;

public class AndroidVideoTrack implements WebRTCMediaTrack {

    private VideoTrack videoTrack;
    private AndroidMediaStream stream;

    public AndroidVideoTrack(VideoTrack videoTrack, AndroidMediaStream stream) {
        this.videoTrack = videoTrack;
        this.stream = stream;
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
        if (stream.getStream().videoTracks.contains(videoTrack)) {
            videoTrack.setEnabled(isEnabled);
        }
    }
}
