package im.actor.core.js.providers.webrtc;

import com.google.gwt.core.client.JavaScriptObject;

public class JsMediaStreamTrack extends JavaScriptObject {

    protected JsMediaStreamTrack() {

    }

    public final native boolean isEnabled()/*-{
        return this.enabled;
    }-*/;

    public final native void stop()/*-{
        this.stop();
    }-*/;
}
