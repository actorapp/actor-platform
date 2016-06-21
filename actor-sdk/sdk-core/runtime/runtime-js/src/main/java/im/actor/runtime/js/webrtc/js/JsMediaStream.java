package im.actor.runtime.js.webrtc.js;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class JsMediaStream extends JavaScriptObject {

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

    public final void stop() {
        JsArray<JsMediaStreamTrack> tracks = getTracks();
        for (int i = 0; i < tracks.length(); i++) {
            tracks.get(i).stop();
        }
    }

    public native final String createUrl()/*-{
        return URL.createObjectURL(this);
    }-*/;

    public final void stopAll() {
        JsArray<JsMediaStreamTrack> tracks = getTracks();
        for (int i = 0; i < tracks.length(); i++) {
            tracks.get(i).setEnabled(false);
        }
    }

    public final void startAll() {
        JsArray<JsMediaStreamTrack> tracks = getTracks();
        for (int i = 0; i < tracks.length(); i++) {
            tracks.get(i).setEnabled(true);
        }
    }
}
