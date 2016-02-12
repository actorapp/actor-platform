package im.actor.runtime.js.webrtc;

import com.google.gwt.core.client.JavaScriptObject;

public class JsSessionDescription extends JavaScriptObject {

    public static native JsSessionDescription create(String type, String sdp)/*-{
        return new RTCSessionDescription({type: type, sdp: sdp});
    }-*/;

    public static native JsSessionDescription createOffer(String sdp)/*-{
        return new RTCSessionDescription({type: 'offer', sdp: sdp});
    }-*/;

    public static native JsSessionDescription createAnswer(String sdp)/*-{
        return new RTCSessionDescription({type: 'answer', sdp: sdp});
    }-*/;

    protected JsSessionDescription() {

    }

    public final native String getSDP()/*-{
        return this.sdp;
    }-*/;

    public final native String getType()/*-{
        return this.type;
    }-*/;
}
