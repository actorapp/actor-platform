package im.actor.runtime.js.webrtc;

import im.actor.runtime.webrtc.WebRTCMediaStream;

public class MediaStream implements WebRTCMediaStream {

    private JsMediaStream stream;
    private JsAudio audio;
    private boolean isEnabled = true;

    public MediaStream(JsMediaStream stream) {
        this(stream, true);
    }

    public MediaStream(JsMediaStream stream, boolean autoPlay) {
        this.stream = stream;
        if (autoPlay) {
            this.audio = JsAudio.create();
            this.audio.setStream(stream);
            this.audio.play();
        }
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public void setEnabled(boolean isEnabled) {
        if (isEnabled) {
            audio.play();
        } else {
            audio.pause();
        }
    }

    public void dispose() {
        audio.reset();
    }
}
