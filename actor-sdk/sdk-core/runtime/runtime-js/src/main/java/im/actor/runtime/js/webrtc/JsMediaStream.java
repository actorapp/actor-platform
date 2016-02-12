package im.actor.runtime.js.webrtc;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

import im.actor.runtime.webrtc.WebRTCLocalStream;

public class JsMediaStream extends JavaScriptObject implements WebRTCLocalStream {

    protected JsMediaStream() {

    }

    public native final JsArray<JsMediaStreamTrack> getTracks()/*-{
        return this.getTracks();
    }-*/;

    public native final JsArray<JsMediaStreamTrack> getAudioTracks()/*-{
        return this.getAudioTracks();
    }-*/;

    public native final JsArray<JsMediaStreamTrack> getVideoTracks()/*-{
        return this.getVideoTracks();
    }-*/;

    public native final void addTrack(JsMediaStreamTrack track)/*-{
        this.addTrack(track);
    }-*/;

    public native final void removeTrack(JsMediaStreamTrack track)/*-{
        this.removeTrack(track);
    }-*/;

    public final void stopAll() {
        JsArray<JsMediaStreamTrack> tracks = getTracks();
        for (int i = 0; i < tracks.length(); i++) {
            tracks.get(i).stop();
        }
    }
}
