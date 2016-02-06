package im.actor.core.js.providers.webrtc;

import com.google.gwt.core.client.JavaScriptObject;

public class JsRTCIceCandidate extends JavaScriptObject {

    public native static JsRTCIceCandidate create(String candidate)/*-{
        return new RTCIceCandidate(candidate);
    }-*/;

    protected JsRTCIceCandidate() {

    }
}
