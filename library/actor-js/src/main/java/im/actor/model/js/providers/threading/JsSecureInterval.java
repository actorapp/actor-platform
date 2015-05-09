/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.providers.threading;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * setInterval/setTimeout implementation
 * that work in background tabs
 */
public class JsSecureInterval extends JavaScriptObject {
    public static native JsSecureInterval create(Runnable runnable)/*-{
//        if (typeof(Worker) !== "undefined") {
//            worker = new Worker("js/interval.js");
//            _runnable = runnable;
//            worker.onmessage = function() {
//                console.log("Worker: on Message");
//                _runnable.@java.lang.Runnable::run()();
//            };
//            return {runnable: runnable, useWebWorker: true, worker: worker};
//        } else {
//            return {runnable: runnable, useWebWorker: false};
//        }
          return {runnable: runnable, useWebWorker: false};
    }-*/;

    protected JsSecureInterval() {

    }

    public final void scheduleNow() {
        schedule(0);
    }

    public native final void schedule(int msec)/*-{
        console.log("JsSecureInterval: schedule at " + msec);
        if (this.useWebWorker) {
            this.worker.postMessage({message: "schedule", delay: msec});
        } else {
            if (this.timerId) {
                clearTimeout(this.timerId);
                this.timerId = null;
            }
            _runnable = this.runnable
            this.timerId = setTimeout(function() {
                _runnable.@java.lang.Runnable::run()();
            }, msec);
        }
    }-*/;

    public native final void cancel()/*-{
        console.log("JsSecureInterval: cancel");
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
