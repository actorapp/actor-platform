package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

public class JsAttachField extends JavaScriptObject {

    public static native JsAttachField create(String title, String value, boolean isShort)/*-{
        return {title: title, value: value, isShort: isShort};
    }-*/;

    protected JsAttachField() {

    }
}