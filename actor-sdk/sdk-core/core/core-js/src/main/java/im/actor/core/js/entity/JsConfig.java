package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

public class JsConfig extends JavaScriptObject {

    public final native String[] getEndpoints()/*-{
        return this.endpoints;
    }-*/;

    public final native JsLogCallback getLogHandler()/*-{
        return this.logHandler;
    }-*/;

    protected JsConfig() {

    }
}