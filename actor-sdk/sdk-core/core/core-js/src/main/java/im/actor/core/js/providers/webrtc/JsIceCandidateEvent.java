package im.actor.core.js.providers.webrtc;

import com.google.gwt.core.client.JavaScriptObject;

public class JsIceCandidateEvent extends JavaScriptObject {
    protected JsIceCandidateEvent() {

    }

    public final native String getId()/*-{
        if (this.id === undefined) {
            return "";
        }
        return this.id;
    }-*/;

    public final native int getLabel()/*-{
        return this.label;
    }-*/;

    public final native String getSDP()/*-{
        return this.sdp;
    }-*/;
}
