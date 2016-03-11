package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

public class JsLogCallback extends JavaScriptObject {

    protected JsLogCallback() {

    }

    public final native void log(String tag, String level, String message)/*-{
        this(tag, level, message);
    }-*/;
}
