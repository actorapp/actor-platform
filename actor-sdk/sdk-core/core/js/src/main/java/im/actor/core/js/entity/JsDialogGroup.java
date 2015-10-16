package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class JsDialogGroup extends JavaScriptObject {

    public static native JsDialogGroup create(String title, String key, JsArray<JsDialogShort> shorts)/*-{
        return {title: title, key: key, shorts: shorts };
    }-*/;

    protected JsDialogGroup() {
    }
}
