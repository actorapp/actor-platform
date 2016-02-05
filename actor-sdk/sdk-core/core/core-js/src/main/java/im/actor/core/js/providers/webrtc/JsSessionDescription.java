package im.actor.core.js.providers.webrtc;

import com.google.gwt.core.client.JavaScriptObject;

public class JsSessionDescription extends JavaScriptObject {

    protected JsSessionDescription() {

    }

    public final native String getSDP()/*-{
        return this.sdp;
    }-*/;

    public final native String getType()/*-{
        return this.type;
    }-*/;
}
