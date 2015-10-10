package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

public class JsCounter extends JavaScriptObject {

    public static native JsCounter create(int counter)/*-{
        return {counter: counter};
    }-*/;


    protected JsCounter() {

    }
}
