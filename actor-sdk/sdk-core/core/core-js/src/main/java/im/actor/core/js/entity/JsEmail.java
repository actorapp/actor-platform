package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

public class JsEmail extends JavaScriptObject {

    public native static JsEmail create(String email, String title)/*-{
        return {email: email, title: title};
    }-*/;

    protected JsEmail() {

    }
}
