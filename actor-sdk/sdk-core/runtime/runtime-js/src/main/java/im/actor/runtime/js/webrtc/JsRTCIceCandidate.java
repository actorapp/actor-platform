package im.actor.runtime.js.webrtc;

import com.google.gwt.core.client.JavaScriptObject;

public class JsRTCIceCandidate extends JavaScriptObject {

    public native static JsRTCIceCandidate create(String candidate)/*-{
        return new RTCIceCandidate(candidate);
    }-*/;

    protected JsRTCIceCandidate() {

    }

    public final native String getId()/*-{
        return this.sdpMLineIndex;
    }-*/;

    public final native int getLabel()/*-{
        return this.sdpMid;
    }-*/;

    public final native String getSDP()/*-{
        return this.candidate;
    }-*/;
}
