/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js.threading;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * setInterval/setTimeout implementation
 * that work in background tabs
 */
public class JsSecureInterval extends JavaScriptObject {
    public static native JsSecureInterval create(Runnable runnable)/*-{
        console.warn("Create jsSecureInverval")
        try {
            if (typeof(Worker) !== "undefined") {
                var worker = new Worker("actor/interval.js");
                var _runnable = runnable;
                worker.onmessage = function() {
                    _runnable.@java.lang.Runnable::run()();
                };
                return {runnable: runnable, useWebWorker: true, worker: worker};
            }
        } catch (e) {
            // Ignore
        }

        return {runnable: runnable, useWebWorker: false};
    }-*/;

    protected JsSecureInterval() {

    }

    public final void scheduleNow() {
        schedule(0);
    }

    public native final void schedule(int msec)/*-{
        if (this.useWebWorker) {
            this.worker.postMessage({message: "schedule", delay: msec});
        } else {
            if (this.timerId) {
                clearTimeout(this.timerId);
                this.timerId = null;
            }
            var _runnable = this.runnable
            this.timerId = setTimeout(function() {
                _runnable.@java.lang.Runnable::run()();
            }, msec);
        }
    }-*/;

    public native final void cancel()/*-{
        if (this.useWebWorker) {
            this.worker.postMessage({message: "cancel"});
        } else {
            if (this.timerId) {
                clearTimeout(this.timerId);
                this.timerId = null;
            }
        }
    }-*/;
}
