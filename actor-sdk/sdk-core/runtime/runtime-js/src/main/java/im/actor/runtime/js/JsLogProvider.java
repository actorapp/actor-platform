/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js;

import com.google.gwt.i18n.client.DateTimeFormat;

import java.util.Date;

import im.actor.runtime.LogRuntime;

public class JsLogProvider implements LogRuntime {

    private static DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat("HH:mm:ss.SSSS");

    private String formatTime() {
        return dateTimeFormat.format(new Date());
    }

    @Override
    public void w(String tag, String message) {
        warn(formatTime() + "[W] " + tag + ":" + message);
    }

    @Override
    public void e(String tag, Throwable throwable) {
        error(formatTime() + "[E] " + tag + ":" + throwable);
    }

    @Override
    public void d(String tag, String message) {
        log(formatTime() + "[D] " + tag + ":" + message);
    }

    @Override
    public void v(String tag, String message) {
        info(formatTime() + "[V] " + tag + ":" + message);
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