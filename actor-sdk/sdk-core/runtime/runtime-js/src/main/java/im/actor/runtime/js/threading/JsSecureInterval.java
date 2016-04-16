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

    public static native JsSecureInterval create(boolean allowWorkers, Runnable runnable)/*-{
        console.warn("Create jsSecureInterval")
        try {
            if (allowWorkers && typeof(Worker) !== "undefined") {

                var code = "var timerId;\n" +
                "\n" +
                "self.addEventListener('message', function(e){\n" +
                "    switch (e.data.message) {\n" +
                "        case 'schedule':\n" +
                "            if (timerId) {\n" +
                "                clearTimeout(timerId);\n" +
                "                timerId = null;\n" +
                "            }\n" +
                "            timerId = setTimeout(function(){\n" +
                "                self.postMessage('doSchedule');\n" +
                "            }, e.data.delay);\n" +
                "            break;\n" +
                "        case 'cancel':\n" +
                "            if (timerId) {\n" +
                "                clearTimeout(timerId);\n" +
                "                timerId = null;\n" +
                "            }\n" +
                "            break;\n" +
                "    };\n" +
                "});\n"

                var codeBlob = new Blob([code]);
                var codeBlobURL = window.URL.createObjectURL(codeBlob);

                var worker = new Worker(codeBlobURL);
                var _runnable = runnable;
                worker.addEventListener('error', function() {
                    console.warn("Worker error")
                }, false);
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
