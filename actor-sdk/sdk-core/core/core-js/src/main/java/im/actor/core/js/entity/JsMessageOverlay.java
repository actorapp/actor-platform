package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

public class JsMessageOverlay extends JavaScriptObject {

    public static native JsMessageOverlay create(boolean useShort, String dateDivider)/*-{
        return {useShort: useShort, dateDivider: dateDivider};
    }-*/;

    protected JsMessageOverlay() {

    }
}
