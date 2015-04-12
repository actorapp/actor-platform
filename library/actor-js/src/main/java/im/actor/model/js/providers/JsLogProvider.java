package im.actor.model.js.providers;

import im.actor.model.LogProvider;

/**
 * Created by ex3ndr on 21.02.15.
 */
public class JsLogProvider implements LogProvider {
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

    public static native void error(String message) /*-{
        window.console.error(message);
    }-*/;

    public static native void warn(String message) /*-{
        window.console.warn(message);
    }-*/;

    public static native void info(String message) /*-{
        window.console.info(message);
    }-*/;

    public static native void log(String message) /*-{
        window.console.log(message);
    }-*/;
}