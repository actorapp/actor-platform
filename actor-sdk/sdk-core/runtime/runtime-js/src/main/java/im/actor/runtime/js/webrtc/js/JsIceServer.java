package im.actor.runtime.js.webrtc.js;

import com.google.gwt.core.client.JavaScriptObject;

public class JsIceServer extends JavaScriptObject {

    public static native JsIceServer create(String url)/*-{
        return {url: url};
    }-*/;

    public static native JsIceServer create(String url, String userName, String credential)/*-{
        return {url: url, username: userName, credential: credential};
    }-*/;

    protected JsIceServer() {

    }
}
