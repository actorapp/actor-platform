package im.actor.runtime.js.webrtc;

import im.actor.runtime.js.media.JsAudio;
import im.actor.runtime.webrtc.WebRTCMediaStream;

public class MediaStream implements WebRTCMediaStream {

    private JsMediaStream stream;
    private JsAudio audio;
    private boolean isAudioEnabled = true;

    public MediaStream(JsMediaStream stream) {
        this(stream, true);
    }

    public MediaStream(JsMediaStream stream, boolean autoPlay) {
        this.stream = stream;
        if (autoPlay) {
            this.audio = JsAudio.create();
            this.audio.setSourceStream(stream);
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
    public boolean isAudioEnabled() {
        return isAudioEnabled;
    }

    @Override
    public void setAudioEnabled(boolean isEnabled) {
        if (audio != null) {
            if (isEnabled) {
                audio.play();
            } else {
                audio.pause();
            }
        }
        if (!isEnabled) {
            stream.stopAll();
        } else {
            stream.startAll();
        }
    }

    @Override
    public boolean isVideoEnabled() {
        //TODO implement
        return false;
    }

    @Override
    public void setVideoEnabled(boolean isEnabled) {
        //TODO implement
    }

    @Override
    public void close() {
        if (audio != null) {
            audio.pause();
            audio.reset();
        }
        stream.stop();
    }
}
