package im.actor.runtime.js.webrtc;

import im.actor.runtime.js.media.JsAudio;
import im.actor.runtime.js.webrtc.js.JsMediaStream;
import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCMediaTrack;
import im.actor.runtime.webrtc.WebRTCTrackType;

public class MediaStream implements WebRTCMediaStream {

    private JsMediaStream stream;
    private JsAudio audio;
    private WebRTCMediaTrack[] audioTracks;
    private WebRTCMediaTrack[] videoTracks;
    private WebRTCMediaTrack[] allTracks;

    public MediaStream(JsMediaStream stream) {
        this(stream, true);
    }

    public MediaStream(JsMediaStream stream, boolean autoPlay) {
        this.stream = stream;

        this.audioTracks = new WebRTCMediaTrack[this.stream.getAudioTracks().length()];
        this.videoTracks = new WebRTCMediaTrack[this.stream.getVideoTracks().length()];
        this.allTracks = new WebRTCMediaTrack[audioTracks.length + videoTracks.length];
        for (int i = 0; i < audioTracks.length; i++) {
            audioTracks[i] = new MediaTrack(stream.getAudioTracks().get(i), WebRTCTrackType.AUDIO);
            allTracks[i] = audioTracks[i];
        }
        for (int i = 0; i < videoTracks.length; i++) {
            videoTracks[i] = new MediaTrack(stream.getVideoTracks().get(i), WebRTCTrackType.VIDEO);
            allTracks[i + audioTracks.length] = videoTracks[i];
        }

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
        if (audio != null) {
            audio.pause();
            audio.reset();
        }
        stream.stop();
    }
}
