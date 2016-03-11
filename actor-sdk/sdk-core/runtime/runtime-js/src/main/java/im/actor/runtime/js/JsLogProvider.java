/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js;

import com.google.gwt.i18n.client.DateTimeFormat;

import java.util.Date;

import im.actor.runtime.LogRuntime;

public class JsLogProvider implements LogRuntime {

    private static LogCallback logCallback;

    public static LogCallback getLogCallback() {
        return logCallback;
    }

    public static void setLogCallback(LogCallback logCallback) {
        JsLogProvider.logCallback = logCallback;
    }

    private static DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat("HH:mm:ss.SSSS");

    private String formatTime() {
        return dateTimeFormat.format(new Date());
    }

    @Override
    public void w(String tag, String message) {
        if (logCallback != null) {
            logCallback.log(tag, "w", message);
        } else {
            warn(formatTime() + "[W] " + tag + ":" + message);
        }

    }

    @Override
    public void e(String tag, Throwable throwable) {
        String stackTrace = "";
        for (StackTraceElement element : throwable.getStackTrace()) {
            stackTrace += element + "\n";
        }

        if (logCallback != null) {
            logCallback.log(tag, "e", throwable.getMessage() + "\n" + stackTrace);
        } else {
            error(formatTime() + "[E] " + tag + ":" + throwable.getMessage() + "\n" + stackTrace);
        }
    }

    @Override
    public void d(String tag, String message) {
        if (logCallback != null) {
            logCallback.log(tag, "d", message);
        } else {
            log(formatTime() + "[D] " + tag + ":" + message);
        }
    }

    @Override
    public void v(String tag, String message) {
        if (logCallback != null) {
            logCallback.log(tag, "v", message);
        } else {
            info(formatTime() + "[V] " + tag + ":" + message);
        }
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

    public interface LogCallback {
        void log(String tag, String level, String message);
    }
}