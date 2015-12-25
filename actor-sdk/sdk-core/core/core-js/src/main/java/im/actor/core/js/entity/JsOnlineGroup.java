package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

public class JsOnlineGroup extends JavaScriptObject {

    public static native JsOnlineGroup create(int total, int online, String message, boolean isNotMember)/*-{
        return {total: total, online: online, message: message, isNotMember: isNotMember};
    }-*/;

    protected JsOnlineGroup() {

    }
}
