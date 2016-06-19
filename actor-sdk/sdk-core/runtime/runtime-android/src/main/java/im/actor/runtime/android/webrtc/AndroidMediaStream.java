package im.actor.runtime.android.webrtc;

import org.webrtc.MediaStream;

import im.actor.runtime.webrtc.WebRTCMediaStream;

public class AndroidMediaStream implements WebRTCMediaStream {

    private MediaStream stream;

    public AndroidMediaStream(MediaStream stream) {
        this.stream = stream;
    }


    public MediaStream getStream() {
        return stream;
    }


    @Override
    public void close() {
        stream.dispose();
    }


    //
    // Deprecated
    //

    @Override
    public boolean isAudioEnabled() {
        return true;
    }

    @Override
    public void setAudioEnabled(boolean isEnabled) {

    }

    @Override
    public boolean isVideoEnabled() {
        return true;
    }

    @Override
    public void setVideoEnabled(boolean isEnabled) {

    }
}
