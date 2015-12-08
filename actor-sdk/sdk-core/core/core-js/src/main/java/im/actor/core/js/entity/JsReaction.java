package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;

public class JsReaction extends JavaScriptObject {

    public static native JsReaction create(String reaction, JsArrayInteger uids, boolean isOwnSet)/*-{
        return {reaction: reaction, uids: uids, isOwnSet: isOwnSet};
    }-*/;

    protected JsReaction() {

    }
}
