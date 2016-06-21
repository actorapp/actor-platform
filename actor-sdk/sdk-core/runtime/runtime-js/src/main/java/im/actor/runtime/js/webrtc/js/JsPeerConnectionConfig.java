package im.actor.runtime.js.webrtc.js;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class JsPeerConnectionConfig extends JavaScriptObject {

    public static native JsPeerConnectionConfig create(JsArray<JsIceServer> iceServers)/*-{
        return {iceServers: iceServers};
    }-*/;

    protected JsPeerConnectionConfig() {

    }
}
