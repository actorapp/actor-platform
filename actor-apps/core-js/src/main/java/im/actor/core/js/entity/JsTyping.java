/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

public class JsTyping extends JavaScriptObject {
    public static native JsTyping create(String typing)/*-{
        return {typing: typing};
    }-*/;

    protected JsTyping() {

    }
}
