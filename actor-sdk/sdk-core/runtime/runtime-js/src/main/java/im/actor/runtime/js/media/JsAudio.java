package im.actor.runtime.js.media;

import com.google.gwt.core.client.JavaScriptObject;

import im.actor.runtime.js.webrtc.js.JsMediaStream;

public class JsAudio extends JavaScriptObject {

    public static native JsAudio create()/*-{
        return {audio: $wnd.document.createElement('AUDIO')};
    }-*/;

    protected JsAudio() {

    }

    public final native void setSourceUrl(String url)/*-{
        this.audio.src = url;
    }-*/;

    public final native void setSourceStream(JsMediaStream mediaStream)/*-{
        this.audio.src = URL.createObjectURL(mediaStream);
    }-*/;

    public final native void play()/*-{
        this.audio.play();
    }-*/;

    public final native void pause()/*-{
        this.audio.pause();
    }-*/;

    public final native void reset()/*-{
        this.audio.src = ""
    }-*/;
}
