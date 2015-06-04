/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

public class JsPromise extends JavaScriptObject {

    public static native JsPromise create(JsPromiseExecutor executor)/*-{
        var _executor = executor
        return new Promise(function (resolve, reject) {
            _executor.@im.actor.model.js.entity.JsPromiseExecutor::performExecute(*)(resolve, reject);
        });
    }-*/;

    protected JsPromise() {

    }
}