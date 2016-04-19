package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

public class JsBlockedUser extends JavaScriptObject {

    public static native JsBlockedUser create(boolean isBlocked)/*-{
        return {isBlocked: isBlocked};
    }-*/;

    protected JsBlockedUser() {

    }
}
