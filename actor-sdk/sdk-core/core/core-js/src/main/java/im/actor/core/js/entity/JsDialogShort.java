package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

public class JsDialogShort extends JavaScriptObject {

    public static native JsDialogShort create(JsPeerInfo peer, int counter)/*-{
        return {peer: peer, counter:counter };
    }-*/;

    protected JsDialogShort() {

    }
}
