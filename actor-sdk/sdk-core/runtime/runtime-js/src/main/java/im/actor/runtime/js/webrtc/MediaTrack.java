package im.actor.runtime.js.webrtc;

import im.actor.runtime.js.webrtc.js.JsMediaStreamTrack;
import im.actor.runtime.webrtc.WebRTCMediaTrack;

public class MediaTrack implements WebRTCMediaTrack {

    private JsMediaStreamTrack track;
    private int type;

    public MediaTrack(JsMediaStreamTrack track, int type) {
        this.track = track;
        this.type = type;
    }

    public JsMediaStreamTrack getTrack() {
        return track;
    }

    @Override
    public int getTrackType() {
        return type;
    }

    @Override
    public boolean isEnabled() {
        return track.isEnabled();
    }

    @Override
    public void setEnabled(boolean isEnabled) {
        track.setEnabled(isEnabled);
    }
}
