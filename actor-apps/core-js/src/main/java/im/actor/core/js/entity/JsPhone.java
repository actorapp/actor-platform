/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

public class JsPhone extends JavaScriptObject {
    public native static JsPhone create(String number, String title)/*-{
        return {number: number, title: title};
    }-*/;

    protected JsPhone() {

    }
}
