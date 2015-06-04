/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

public class JsPromise extends JavaScriptObject {

    public static native JsPromise create(JsPromiseExecutor executor)/*-{
        var executor_ = executor
        return new Promise(function (resolve, reject) {
            executor_.@im.actor.model.js.entity.JsPromiseExecutor::execute(*)(resolve, reject);
        });
    }-*/;

    protected JsPromise() {

    }
}