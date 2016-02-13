package im.actor.runtime.js.webrtc;

import com.google.gwt.core.client.JavaScriptObject;

public class JsMediaStreamTrack extends JavaScriptObject {

    protected JsMediaStreamTrack() {

    }

    public final native void setEnabled(boolean val)/*-{
        this.enabled = val;
    }-*/;

    public final native boolean isEnabled()/*-{
        return this.enabled;
    }-*/;

    public final native void stop()/*-{
        this.stop();
    }-*/;
}
