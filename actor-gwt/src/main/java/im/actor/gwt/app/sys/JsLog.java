package im.actor.gwt.app.sys;

import im.actor.model.LogCallback;

/**
 * Created by ex3ndr on 21.02.15.
 */
public class JsLog implements LogCallback {
    @Override
    public void w(String tag, String message) {
        warn("[W]" + tag + ":" + message);
    }

    @Override
    public void e(String tag, Throwable throwable) {
        error("[E]" + tag + ":" + throwable);
    }

    @Override
    public void d(String tag, String message) {
        log("[D]" + tag + ":" + message);
    }

    @Override
    public void v(String tag, String message) {
        info("[V]" + tag + ":" + message);
    }

    private native void error(String message) /*-{
        window.console.error(message);
    }-*/;

    private native void warn(String message) /*-{
        window.console.warn(message);
    }-*/;

    private native void info(String message) /*-{
        window.console.info(message);
    }-*/;

    private native void log(String message) /*-{
        window.console.log(message);
    }-*/;
}