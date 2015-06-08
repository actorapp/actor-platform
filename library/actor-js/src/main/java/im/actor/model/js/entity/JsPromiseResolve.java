/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

public class JsPromiseResolve extends JavaScriptObject {

    protected JsPromiseResolve() {

    }

    public native void execute(Object arg)/*-{
        this(arg);
    }-*/;

    public native void execute()/*-{
        this();
    }-*/;
}
