/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js.utils;

import com.google.gwt.core.client.JavaScriptObject;

public abstract class JsPromiseExecutor {

    private JavaScriptObject resolveFunc;
    private JavaScriptObject rejectFunc;

    public JsPromiseExecutor() {

    }

    public final void performExecute(JavaScriptObject resolve, JavaScriptObject reject) {
        this.resolveFunc = resolve;
        this.rejectFunc = reject;

        execute();
    }

    public abstract void execute();

    protected void resolve() {
        call(resolveFunc);
    }

    protected void resolve(Object arg) {
        call(resolveFunc, arg);
    }

    protected void reject() {
        call(rejectFunc);
    }

    protected void reject(Object arg) {
        call(rejectFunc, arg);
    }

    private native void call(JavaScriptObject func)/*-{
        func();
    }-*/;

    private native void call(JavaScriptObject func, Object arg)/*-{
        func(arg);
    }-*/;
}
