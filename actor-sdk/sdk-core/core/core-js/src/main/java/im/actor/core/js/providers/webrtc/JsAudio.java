package im.actor.core.js.providers.webrtc;

import com.google.gwt.core.client.JavaScriptObject;

public class JsAudio extends JavaScriptObject {

    public static native JsAudio create()/*-{
        return {audio: $wnd.document.createElement('AUDIO')};
    }-*/;

    protected JsAudio() {

    }

    public final native void setStream(JsMediaStream mediaStream)/*-{
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
