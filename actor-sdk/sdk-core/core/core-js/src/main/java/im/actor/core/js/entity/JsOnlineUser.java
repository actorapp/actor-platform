package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

public class JsOnlineUser extends JavaScriptObject {

    public static native JsOnlineUser create(String message, boolean isOnline)/*-{
        return {message: message, isOnline: isOnline};
    }-*/;

    protected JsOnlineUser() {

    }
}
