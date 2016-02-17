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

    public JsMediaStream getStream() {
        return stream;
    }

    public JsAudio getAudio() {
        return audio;
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

    @Override
    public void close() {
        if (audio != null) {
            audio.reset();
        }
        stream.stopAll();
    }
}
